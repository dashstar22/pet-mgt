package com.petmgt.service;

import com.petmgt.config.FileUploadConfig;
import com.petmgt.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final List<String> allowedExtensions;

    public FileStorageService(FileUploadConfig config) {
        Path projectRoot = FileUtil.findProjectRoot();
        this.uploadDir = projectRoot.resolve(config.getUploadDir()).normalize();
        this.allowedExtensions = Arrays.asList(config.getAllowedExtensions().split(","));
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
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("仅支持以下格式: " + String.join(",", allowedExtensions));
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过 2MB");
        }

        String newFileName = UUID.randomUUID().toString() + "." + extension;

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path dest = uploadDir.resolve(newFileName);
        file.transferTo(dest.toFile());

        try {
            FileUtil.createThumbnail(dest.toAbsolutePath().toString());
        } catch (Exception e) {
            System.err.println("WARN: Thumbnail creation failed for " + dest + ": " + e.getMessage());
        }

        return newFileName;
    }

    public void delete(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        try {
            Path file = uploadDir.resolve(fileName);
            Files.deleteIfExists(file);
            Path thumb = uploadDir.resolve("thumb_" + fileName);
            Files.deleteIfExists(thumb);
        } catch (IOException e) {
            // file may already be gone
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? "" : filename.substring(dotIndex + 1);
    }
}
