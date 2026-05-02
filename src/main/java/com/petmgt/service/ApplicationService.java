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
}
