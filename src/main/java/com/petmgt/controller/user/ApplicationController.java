package com.petmgt.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApplicationForm;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.exception.BusinessException;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.service.ApplicationService;
import com.petmgt.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;

    public ApplicationController(ApplicationService applicationService,
                                 ApplicationMapper applicationMapper,
                                 PetMapper petMapper,
                                 BreedMapper breedMapper,
                                 PetImageMapper petImageMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
    }

    @GetMapping("/apply/{petId}")
    public String applicationForm(@PathVariable Long petId, Model model) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            return "redirect:/pets";
        }
        model.addAttribute("title", "提交领养申请");
        model.addAttribute("pet", pet);
        return "user/application-form";
    }

    @PostMapping("/apply")
    public String submitApplication(ApplicationForm form, RedirectAttributes redirectAttributes) {
        try {
            Long userId = SecurityUtil.getCurrentUser().getId();
            applicationService.submit(form, userId);
            redirectAttributes.addFlashAttribute("success", "申请已提交，请等待审核");
            return "redirect:/user/applications";
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @GetMapping("/applications")
    public String myApplications(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Page<Application> appPage = new Page<>(page, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
            .eq(Application::getUserId, userId)
            .orderByDesc(Application::getCreatedAt);
        Page<Application> result = applicationMapper.selectPage(appPage, wrapper);

        for (Application app : result.getRecords()) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                app.setCoverImageUrl(
                    petImageMapper.selectList(new LambdaQueryWrapper<com.petmgt.entity.PetImage>()
                        .eq(com.petmgt.entity.PetImage::getPetId, pet.getId())
                        .eq(com.petmgt.entity.PetImage::getIsCover, 1))
                        .stream().findFirst()
                        .map(com.petmgt.entity.PetImage::getImageUrl).orElse(null));
                var breed = breedMapper.selectById(pet.getBreedId());
                if (breed != null) {
                    app.setBreedName(breed.getBreedName());
                }
            }
        }

        model.addAttribute("title", "我的申请");
        model.addAttribute("page", result);
        return "user/applications";
    }

    @PostMapping("/applications/{id}/cancel")
    public String cancelApplication(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Long userId = SecurityUtil.getCurrentUser().getId();
            applicationService.cancel(id, userId);
            redirectAttributes.addFlashAttribute("success", "申请已取消");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return "redirect:/user/applications";
    }
}
