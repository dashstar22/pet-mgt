package com.petmgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petmgt.dto.RegisterForm;
import com.petmgt.entity.Role;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, RoleMapper roleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterForm form) {
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("两次密码不一致");
        }
        if (form.getEmail() != null && !form.getEmail().isBlank()
                && !form.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        User existing = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, form.getUsername()));
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEmail(form.getEmail());
        user.setEnabled(1);
        userMapper.insert(user);

        Role userRole = roleMapper.selectOne(
            new LambdaQueryWrapper<Role>().eq(Role::getRoleName, "ROLE_USER"));
        if (userRole != null) {
            roleMapper.insertUserRole(user.getId(), userRole.getId());
        }
    }
}
