package com.petmgt.config;

import com.petmgt.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path projectRoot = FileUtil.findProjectRoot();
        Path uploadPath = projectRoot.resolve(uploadDir).normalize();
        Path serveRoot = uploadPath.getParent();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + serveRoot.toString().replace('\\', '/') + "/");
    }
}
