package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Application;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.entity.User;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.dto.AiReviewResult;
import com.petmgt.service.ApplicationService;
import com.petmgt.service.ai.AiReviewService;
import com.petmgt.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/applications")
public class ApplicationController {

    private final ApplicationMapper applicationMapper;
    private final ApplicationService applicationService;
    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;
    private final UserMapper userMapper;
    private final AiReviewService aiReviewService;

    public ApplicationController(ApplicationMapper applicationMapper,
                                  ApplicationService applicationService,
                                  PetMapper petMapper,
                                  BreedMapper breedMapper,
                                  PetImageMapper petImageMapper,
                                  UserMapper userMapper,
                                  AiReviewService aiReviewService) {
        this.applicationMapper = applicationMapper;
        this.applicationService = applicationService;
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
        this.userMapper = userMapper;
        this.aiReviewService = aiReviewService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String petName,
                       @RequestParam(required = false) String applicant,
                       Model model) {
        Page<Application> appPage = new Page<>(page, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Application::getStatus, status);
        }
        if (petName != null && !petName.isEmpty()) {
            List<Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().like(Pet::getName, petName));
            if (pets.isEmpty()) {
                wrapper.eq(Application::getId, -1L);
            } else {
                wrapper.in(Application::getPetId,
                    pets.stream().map(Pet::getId).collect(Collectors.toList()));
            }
        }
        if (applicant != null && !applicant.isEmpty()) {
            List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().like(User::getUsername, applicant));
            if (users.isEmpty()) {
                wrapper.eq(Application::getId, -1L);
            } else {
                wrapper.in(Application::getUserId,
                    users.stream().map(User::getId).collect(Collectors.toList()));
            }
        }
        wrapper.orderByDesc(Application::getCreatedAt);
        Page<Application> result = applicationMapper.selectPage(appPage, wrapper);

        for (Application app : result.getRecords()) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                Breed breed = breedMapper.selectById(pet.getBreedId());
                if (breed != null) app.setBreedName(breed.getBreedName());
            }
            User user = userMapper.selectById(app.getUserId());
            if (user != null) app.setApplicantUsername(user.getUsername());
        }

        model.addAttribute("title", "申请审核");
        model.addAttribute("page", result);
        model.addAttribute("status", status);
        model.addAttribute("petName", petName);
        model.addAttribute("applicant", applicant);
        return "admin/applications";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            return "redirect:/admin/applications";
        }
        Pet pet = petMapper.selectById(app.getPetId());
        if (pet != null) {
            app.setPetName(pet.getName());
            Breed breed = breedMapper.selectById(pet.getBreedId());
            if (breed != null) app.setBreedName(breed.getBreedName());
            List<PetImage> images = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, pet.getId()));
            String coverUrl = images.stream().filter(img -> img.getIsCover() == 1)
                .findFirst().map(PetImage::getImageUrl).orElse(null);
            app.setCoverImageUrl(coverUrl);
        }
        User user = userMapper.selectById(app.getUserId());
        if (user != null) app.setApplicantUsername(user.getUsername());

        model.addAttribute("title", "申请详情");
        model.addAttribute("app", app);
        model.addAttribute("pet", pet);
        model.addAttribute("applicant", user);

        if ("pending".equals(app.getStatus()) && pet != null) {
            try {
                AiReviewResult aiReview = aiReviewService.review(app, pet);
                model.addAttribute("aiReview", aiReview);
            } catch (Exception e) {
                // AI review failed, continue without it
            }
        }

        return "admin/application-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(defaultValue = "") String comment,
                          RedirectAttributes redirectAttributes) {
        try {
            Long adminId = SecurityUtil.getCurrentUser().getId();
            applicationService.approve(id, adminId, comment);
            redirectAttributes.addFlashAttribute("success", "申请已通过");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/applications";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String reason,
                         RedirectAttributes redirectAttributes) {
        try {
            Long adminId = SecurityUtil.getCurrentUser().getId();
            applicationService.reject(id, adminId, reason);
            redirectAttributes.addFlashAttribute("success", "申请已拒绝");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/applications";
    }
}
