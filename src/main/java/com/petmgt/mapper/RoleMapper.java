package com.petmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petmgt.entity.Role;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT r.role_name FROM role r JOIN user_role ur ON r.id = ur.role_id JOIN user u ON ur.user_id = u.id WHERE u.id = #{userId}")
    List<String> findRoleNamesByUserId(Long userId);
}
