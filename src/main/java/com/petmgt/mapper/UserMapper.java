package com.petmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petmgt.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
}
