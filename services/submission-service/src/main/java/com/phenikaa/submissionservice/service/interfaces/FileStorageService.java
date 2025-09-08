package com.phenikaa.submissionservice.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String folderName);
    byte[] downloadFile(String filePath);
    void deleteFile(String filePath);
    String generateFileUrl(String filePath);
    void streamFile(String filePath, OutputStream outputStream);
}
