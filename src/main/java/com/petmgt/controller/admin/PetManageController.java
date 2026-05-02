package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.service.FileStorageService;
import com.petmgt.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/pets")
public class PetManageController {

    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;
    private final FileStorageService fileStorageService;

    public PetManageController(PetMapper petMapper, BreedMapper breedMapper,
                               PetImageMapper petImageMapper,
                               FileStorageService fileStorageService) {
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) Long breedId,
                       @RequestParam(required = false) String status,
                       Model model) {
        Page<Pet> petPage = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Pet::getName, name);
        }
        if (breedId != null) {
            wrapper.eq(Pet::getBreedId, breedId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Pet::getStatus, status);
        }
        wrapper.orderByDesc(Pet::getCreatedAt);
        Page<Pet> result = petMapper.selectPage(petPage, wrapper);

        for (Pet pet : result.getRecords()) {
            Breed breed = breedMapper.selectById(pet.getBreedId());
            if (breed != null) {
                pet.setBreedName(breed.getBreedName());
            }
        }

        model.addAttribute("title", "宠物管理");
        model.addAttribute("page", result);
        model.addAttribute("breeds", breedMapper.selectList(null));
        model.addAttribute("name", name);
        model.addAttribute("breedId", breedId);
        model.addAttribute("status", status);
        return "admin/pets";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("title", "发布宠物");
        model.addAttribute("pet", new Pet());
        model.addAttribute("breeds", breedMapper.selectList(null));
        model.addAttribute("isEdit", false);
        model.addAttribute("petType", "");
        return "admin/pet-form";
    }

    @PostMapping("/create")
    public String create(Pet pet,
                         @RequestParam(required = false) List<MultipartFile> images,
                         @RequestParam(required = false) Integer coverIndex,
                         RedirectAttributes redirectAttributes) {
        pet.setStatus("available");
        pet.setCreatedBy(SecurityUtil.getCurrentUser().getId());
        petMapper.insert(pet);
        if (images != null) {
            saveImages(pet.getId(), images, coverIndex);
        }
        redirectAttributes.addFlashAttribute("success", "宠物发布成功");
        return "redirect:/admin/pets";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return "redirect:/admin/pets";
        }
        Breed breed = breedMapper.selectById(pet.getBreedId());
        model.addAttribute("petType", breed != null ? breed.getPetType() : "");
        List<PetImage> images = petImageMapper.selectList(
            new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        model.addAttribute("title", "编辑宠物");
        model.addAttribute("pet", pet);
        model.addAttribute("breeds", breedMapper.selectList(null));
        model.addAttribute("images", images);
        model.addAttribute("isEdit", true);
        return "admin/pet-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Pet pet,
                       @RequestParam(required = false) List<MultipartFile> newImages,
                       @RequestParam(required = false) Integer coverIndex,
                       @RequestParam(required = false) List<Long> deleteImageIds,
                       RedirectAttributes redirectAttributes) {
        pet.setId(id);
        petMapper.updateById(pet);
        if (deleteImageIds != null) {
            for (Long imageId : deleteImageIds) {
                PetImage img = petImageMapper.selectById(imageId);
                if (img != null) {
                    fileStorageService.delete(img.getImageUrl());
                    petImageMapper.deleteById(imageId);
                }
            }
        }
        if (newImages != null && !newImages.isEmpty()) {
            saveImages(id, newImages, coverIndex);
        }
        redirectAttributes.addFlashAttribute("success", "宠物更新成功");
        return "redirect:/admin/pets";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        List<PetImage> images = petImageMapper.selectList(
            new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        for (PetImage img : images) {
            fileStorageService.delete(img.getImageUrl());
        }
        petImageMapper.delete(new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        petMapper.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "宠物已删除");
        return "redirect:/admin/pets";
    }

    private void saveImages(Long petId, List<MultipartFile> images, Integer coverIndex) {
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file.isEmpty()) continue;
            try {
                String fileName = fileStorageService.store(file);
                PetImage petImage = new PetImage();
                petImage.setPetId(petId);
                petImage.setImageUrl(fileName);
                petImage.setIsCover(coverIndex != null && coverIndex == i + 1 ? 1 : 0);
                petImageMapper.insert(petImage);
            } catch (IOException e) {
                // skip bad files
            }
        }

        if (coverIndex == null) {
            List<PetImage> existing = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, petId));
            if (!existing.isEmpty()) {
                boolean hasCover = existing.stream().anyMatch(img -> img.getIsCover() == 1);
                if (!hasCover) {
                    PetImage first = existing.get(0);
                    first.setIsCover(1);
                    petImageMapper.updateById(first);
                }
            }
        }
    }
}
