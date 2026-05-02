package com.petmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_breed")
public class Breed {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String breedName;
    private String petType;
    private String description;
    private LocalDateTime createdAt;
}
