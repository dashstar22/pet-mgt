package com.petmgt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_image")
public class PetImage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private String imageUrl;
    private Integer isCover;
    private LocalDateTime createdAt;
}
