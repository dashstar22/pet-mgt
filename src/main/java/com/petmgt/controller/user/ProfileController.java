package com.petmgt.controller.user;

import com.petmgt.entity.User;
import com.petmgt.exception.BusinessException;
import com.petmgt.mapper.UserMapper;
import com.petmgt.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class ProfileController {

    private final UserMapper userMapper;

    public ProfileController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = SecurityUtil.getCurrentUser();
        model.addAttribute("title", "个人中心");
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(String email, String avatarUrl) {
        if (email != null && !email.isBlank()
                && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BusinessException("邮箱格式不正确");
        }
        User user = SecurityUtil.getCurrentUser();
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return "redirect:/user/profile";
    }
}
