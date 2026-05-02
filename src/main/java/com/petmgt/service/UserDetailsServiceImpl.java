package com.petmgt.service;

import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public UserDetailsServiceImpl(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null || user.getEnabled() == 0) {
            throw new UsernameNotFoundException("用户不存在或已禁用");
        }
        List<String> roleNames = roleMapper.findRoleNamesByUserId(user.getId());
        List<GrantedAuthority> authorities = roleNames.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), user.getPassword(), authorities);
    }
}
