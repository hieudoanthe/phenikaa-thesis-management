package com.phenikaa.submissionservice.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folderName);
    void deleteFile(String publicId);
    byte[] downloadFile(String publicId);
    String generateSignedUrl(String publicId);
    byte[] downloadFileDirectly(String publicId);
}