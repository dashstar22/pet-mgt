package com.petmgt.controller;

import com.petmgt.dto.RegisterForm;
import com.petmgt.exception.BusinessException;
import com.petmgt.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "登录");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("title", "注册");
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(RegisterForm form) {
        try {
            userService.register(form);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return "redirect:/login?registered";
    }
}
