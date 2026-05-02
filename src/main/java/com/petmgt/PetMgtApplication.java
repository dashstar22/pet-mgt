package com.petmgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.petmgt.mapper")
public class PetMgtApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetMgtApplication.class, args);
    }
}
