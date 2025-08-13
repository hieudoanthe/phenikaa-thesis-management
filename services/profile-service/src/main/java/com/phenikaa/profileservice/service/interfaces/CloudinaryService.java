package com.phenikaa.profileservice.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folderName);
}
