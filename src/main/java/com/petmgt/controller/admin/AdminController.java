package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final ApplicationMapper applicationMapper;

    public AdminController(UserMapper userMapper, PetMapper petMapper,
                           ApplicationMapper applicationMapper) {
        this.userMapper = userMapper;
        this.petMapper = petMapper;
        this.applicationMapper = applicationMapper;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "后台管理");
        model.addAttribute("totalUsers", userMapper.selectCount(null));
        model.addAttribute("totalPets", petMapper.selectCount(null));
        model.addAttribute("availablePets", petMapper.selectCount(
            new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "available")));
        model.addAttribute("adoptedPets", petMapper.selectCount(
            new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "adopted")));
        model.addAttribute("pendingApps", applicationMapper.selectCount(
            new LambdaQueryWrapper<Application>().eq(Application::getStatus, "pending")));
        return "admin/index";
    }
}
