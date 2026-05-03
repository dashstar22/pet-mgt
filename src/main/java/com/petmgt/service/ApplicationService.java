package com.petmgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petmgt.dto.ApplicationForm;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.PetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final PetMapper petMapper;

    public ApplicationService(ApplicationMapper applicationMapper, PetMapper petMapper) {
        this.applicationMapper = applicationMapper;
        this.petMapper = petMapper;
    }

    @Transactional
    public void submit(ApplicationForm form, Long userId) {
        Pet pet = petMapper.selectById(form.getPetId());
        if (pet == null) {
            throw new IllegalArgumentException("宠物不存在");
        }
        if (!"available".equals(pet.getStatus())) {
            throw new IllegalArgumentException("该宠物当前不可申请");
        }

        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
            .eq(Application::getUserId, userId)
            .eq(Application::getPetId, form.getPetId())
            .eq(Application::getStatus, "pending"));
        if (count > 0) {
            throw new IllegalArgumentException("您已提交过该宠物的领养申请，请勿重复申请");
        }

        Application app = new Application();
        app.setPetId(form.getPetId());
        app.setUserId(userId);
        app.setPhone(form.getPhone());
        app.setAddress(form.getAddress());
        app.setExperience(form.getExperience());
        app.setAccompanyTime(form.getAccompanyTime());
        app.setReason(form.getReason());
        app.setStatus("pending");
        applicationMapper.insert(app);

        pet.setStatus("pending");
        petMapper.updateById(pet);
    }

    @Transactional
    public void cancel(Long applicationId, Long userId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        if (!"pending".equals(app.getStatus())) {
            throw new IllegalArgumentException("只能取消待审核状态的申请");
        }
        app.setStatus("cancelled");
        applicationMapper.updateById(app);

        Long pendingCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
            .eq(Application::getPetId, app.getPetId())
            .eq(Application::getStatus, "pending"));
        if (pendingCount == 0) {
            Pet pet = petMapper.selectById(app.getPetId());
            pet.setStatus("available");
            petMapper.updateById(pet);
        }
    }

    @Transactional
    public void approve(Long applicationId, Long adminId, String comment) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (!"pending".equals(app.getStatus())) {
            throw new IllegalArgumentException("该申请已审核过");
        }

        app.setStatus("approved");
        app.setReviewedBy(adminId);
        app.setReviewedAt(java.time.LocalDateTime.now());
        app.setReviewComment(comment);
        applicationMapper.updateById(app);

        Pet pet = petMapper.selectById(app.getPetId());
        pet.setStatus("adopted");
        petMapper.updateById(pet);

        applicationMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Application>()
            .eq(Application::getPetId, app.getPetId())
            .eq(Application::getStatus, "pending")
            .ne(Application::getId, applicationId)
            .set(Application::getStatus, "rejected")
            .set(Application::getReviewComment, "该宠物已被领养，系统自动拒绝")
            .set(Application::getReviewedBy, adminId)
            .set(Application::getReviewedAt, java.time.LocalDateTime.now()));
    }

    @Transactional
    public void deleteById(Long applicationId, Long userId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作");
        }
        if ("approved".equals(app.getStatus())) {
            throw new IllegalArgumentException("已通过的申请不可删除");
        }

        Long petId = app.getPetId();
        String status = app.getStatus();
        applicationMapper.deleteById(applicationId);

        if ("pending".equals(status)) {
            revertPetIfNoPending(petId);
        }
    }

    @Transactional
    public void adminDeleteById(Long applicationId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("申请不存在");
        }

        Long petId = app.getPetId();
        String status = app.getStatus();
        applicationMapper.deleteById(applicationId);

        if ("pending".equals(status)) {
            revertPetIfNoPending(petId);
        }
    }

    @Transactional
    public void deleteAllByUserId(Long userId) {
        var wrapper = new LambdaQueryWrapper<Application>()
            .eq(Application::getUserId, userId);
        java.util.List<Application> userApps = applicationMapper.selectList(wrapper);
        if (userApps.isEmpty()) {
            throw new IllegalArgumentException("没有可删除的申请记录");
        }

        java.util.List<Application> toDelete = userApps.stream()
            .filter(a -> !"approved".equals(a.getStatus()))
            .collect(java.util.stream.Collectors.toList());
        if (toDelete.isEmpty()) {
            throw new IllegalArgumentException("已通过的申请不可删除，无可清理的记录");
        }

        java.util.Set<Long> pendingPetIds = toDelete.stream()
            .filter(a -> "pending".equals(a.getStatus()))
            .map(Application::getPetId)
            .collect(java.util.stream.Collectors.toSet());

        for (Application app : toDelete) {
            applicationMapper.deleteById(app.getId());
        }

        for (Long petId : pendingPetIds) {
            revertPetIfNoPending(petId);
        }
    }

    private void revertPetIfNoPending(Long petId) {
        Long pendingCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
            .eq(Application::getPetId, petId)
            .eq(Application::getStatus, "pending"));
        if (pendingCount == 0) {
            Pet pet = petMapper.selectById(petId);
            if (pet != null) {
                pet.setStatus("available");
                petMapper.updateById(pet);
            }
        }
    }

    @Transactional
    public void reject(Long applicationId, Long adminId, String reason) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (!"pending".equals(app.getStatus())) {
            throw new IllegalArgumentException("该申请已审核过");
        }

        app.setStatus("rejected");
        app.setReviewedBy(adminId);
        app.setReviewedAt(java.time.LocalDateTime.now());
        app.setReviewComment(reason);
        applicationMapper.updateById(app);

        Long pendingCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
            .eq(Application::getPetId, app.getPetId())
            .eq(Application::getStatus, "pending"));
        if (pendingCount == 0) {
            Pet pet = petMapper.selectById(app.getPetId());
            pet.setStatus("available");
            petMapper.updateById(pet);
        }
    }
}
