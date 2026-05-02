package com.petmgt.util;

import com.petmgt.entity.User;
import com.petmgt.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private static UserMapper userMapper;

    public SecurityUtil(UserMapper userMapper) {
        SecurityUtil.userMapper = userMapper;
    }

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        return userMapper.findByUsername(username);
    }
}
