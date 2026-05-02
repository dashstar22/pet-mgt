package com.petmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.PetSearchCriteria;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.service.PetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PetController {

    private final PetService petService;
    private final BreedMapper breedMapper;

    public PetController(PetService petService, BreedMapper breedMapper) {
        this.petService = petService;
        this.breedMapper = breedMapper;
    }

    @GetMapping("/pets")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "12") int size,
                       PetSearchCriteria criteria,
                       Model model) {
        Page<Pet> petPage = petService.findPets(new Page<>(page, size), criteria);
        List<Breed> breeds = breedMapper.selectList(null);

        model.addAttribute("title", "宠物列表");
        model.addAttribute("petPage", petPage);
        model.addAttribute("breeds", breeds);
        model.addAttribute("criteria", criteria);
        return "pet/list";
    }

    @GetMapping("/pets/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Pet pet = petService.findPetDetail(id);
        if (pet == null) {
            return "redirect:/pets";
        }
        List<PetImage> images = petService.findPetImages(id);

        model.addAttribute("title", pet.getName() + " - 宠物详情");
        model.addAttribute("pet", pet);
        model.addAttribute("images", images);
        return "pet/detail";
    }
}
