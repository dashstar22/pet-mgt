package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Breed;
import com.petmgt.exception.BusinessException;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.service.BreedService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/breeds")
public class BreedController {

    private final BreedService breedService;
    private final BreedMapper breedMapper;

    public BreedController(BreedService breedService, BreedMapper breedMapper) {
        this.breedService = breedService;
        this.breedMapper = breedMapper;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String petType,
                       Model model) {
        Page<Breed> result = breedService.list(page, size, petType);
        model.addAttribute("title", "品种管理");
        model.addAttribute("page", result);
        model.addAttribute("petType", petType);
        return "admin/breeds";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("title", "新增品种");
        model.addAttribute("breed", new Breed());
        model.addAttribute("isEdit", false);
        return "admin/breed-form";
    }

    @PostMapping("/create")
    public String create(Breed breed, RedirectAttributes redirectAttributes) {
        breedService.save(breed);
        redirectAttributes.addFlashAttribute("success", "品种创建成功");
        return "redirect:/admin/breeds";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Breed breed = breedMapper.selectById(id);
        if (breed == null) {
            return "redirect:/admin/breeds";
        }
        model.addAttribute("title", "编辑品种");
        model.addAttribute("breed", breed);
        model.addAttribute("isEdit", true);
        return "admin/breed-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Breed breed, RedirectAttributes redirectAttributes) {
        breed.setId(id);
        breedService.update(breed);
        redirectAttributes.addFlashAttribute("success", "品种更新成功");
        return "redirect:/admin/breeds";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            breedService.delete(id);
            redirectAttributes.addFlashAttribute("success", "品种已删除");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return "redirect:/admin/breeds";
    }
}
