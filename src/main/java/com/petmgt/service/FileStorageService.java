package com.petmgt.service;

import com.petmgt.config.FileUploadConfig;
import com.petmgt.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final FileUploadConfig config;

    public FileStorageService(FileUploadConfig config) {
        this.config = config;
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getExtension(originalFilename).toLowerCase();
        List<String> allowed = Arrays.asList(config.getAllowedExtensions().split(","));
        if (!allowed.contains(extension)) {
            throw new IllegalArgumentException("仅支持以下格式: " + config.getAllowedExtensions());
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过 2MB");
        }

        String newFileName = UUID.randomUUID().toString() + "." + extension;
        File uploadDir = new File(config.getUploadDir());
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File dest = new File(uploadDir, newFileName);
        file.transferTo(dest);

        try {
            FileUtil.createThumbnail(dest.getAbsolutePath());
        } catch (IOException e) {
            // thumbnail failure doesn't block upload
        }

        return newFileName;
    }

    public void delete(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        File dir = new File(config.getUploadDir());
        File file = new File(dir, fileName);
        if (file.exists()) file.delete();
        File thumb = new File(dir, "thumb_" + fileName);
        if (thumb.exists()) thumb.delete();
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? "" : filename.substring(dotIndex + 1);
    }
}
