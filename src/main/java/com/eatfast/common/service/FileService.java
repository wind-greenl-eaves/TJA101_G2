package com.eatfast.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.upload.employee-photos}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    
    // 支援的 MIME 類型
    private static final String[] ALLOWED_MIME_TYPES = {
        "image/jpeg", 
        "image/png", 
        "image/gif"
    };

    public String saveEmployeePhoto(MultipartFile file) throws IOException {
        validateFile(file);
        
        // 確保上傳目錄存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一的檔案名稱
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

        // 儲存檔案
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        return newFilename;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("檔案不能為空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("檔案大小不能超過 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        boolean isValidExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (originalFilename.toLowerCase().endsWith(ext)) {
                isValidExtension = true;
                break;
            }
        }

        if (!isValidExtension) {
            throw new IllegalArgumentException("只允許上傳 JPG, JPEG, PNG 或 GIF 格式的圖片");
        }

        String mimeType = file.getContentType();
        boolean isValidMimeType = false;
        for (String mime : ALLOWED_MIME_TYPES) {
            if (mimeType.equals(mime)) {
                isValidMimeType = true;
                break;
            }
        }

        if (!isValidMimeType) {
            throw new IllegalArgumentException("不支援的檔案類型，請上傳圖片檔案");
        }
    }

    public void deleteEmployeePhoto(String filename) {
        if (filename != null && !filename.isEmpty()) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 記錄錯誤但不拋出異常，因為這是清理操作
                e.printStackTrace();
            }
        }
    }
}