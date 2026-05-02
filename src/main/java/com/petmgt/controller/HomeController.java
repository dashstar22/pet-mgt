package com.petmgt.controller;

import com.petmgt.entity.Pet;
import com.petmgt.service.PetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final PetService petService;

    public HomeController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Pet> latestPets = petService.findLatestPets(8);
        model.addAttribute("title", "首页");
        model.addAttribute("latestPets", latestPets);
        return "home";
    }
}
