package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper, RoleMapper roleMapper,
                          PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
            .orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(userPage, wrapper);

        Map<Long, List<String>> roleNames = new LinkedHashMap<>();
        for (User user : result.getRecords()) {
            user.setPassword(null);
            roleNames.put(user.getId(), roleMapper.findRoleNamesByUserId(user.getId()));
        }

        model.addAttribute("title", "用户管理");
        model.addAttribute("page", result);
        model.addAttribute("roleNames", roleNames);
        return "admin/users";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("title", "新增用户");
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleMapper.selectList(null));
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    @PostMapping("/create")
    public String create(User user, @RequestParam(required = false) List<Long> roleIds,
                         RedirectAttributes redirectAttributes) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            redirectAttributes.addFlashAttribute("error", "用户名已存在");
            return "redirect:/admin/users/create";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(user.getEnabled() != null ? user.getEnabled() : 1);
        userMapper.insert(user);
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                roleMapper.insertUserRole(user.getId(), roleId);
            }
        }
        redirectAttributes.addFlashAttribute("success", "用户创建成功");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        user.setPassword(null);
        List<String> currentRoles = roleMapper.findRoleNamesByUserId(id);
        model.addAttribute("title", "编辑用户");
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleMapper.selectList(null));
        model.addAttribute("currentRoleNames", currentRoles);
        model.addAttribute("isEdit", true);
        return "admin/user-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, User user,
                       @RequestParam(required = false) List<Long> roleIds,
                       RedirectAttributes redirectAttributes) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "不能编辑自己的账号");
            return "redirect:/admin/users/" + id + "/edit";
        }
        user.setId(id);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
        redirectAttributes.addFlashAttribute("success", "用户更新成功");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "不能删除自己的账号");
            return "redirect:/admin/users";
        }
        userMapper.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "用户已删除");
        return "redirect:/admin/users";
    }
}
