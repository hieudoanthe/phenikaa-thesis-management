package com.phenikaa.submissionservice.service.adapter;

import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import com.phenikaa.submissionservice.service.interfaces.CloudinaryService;
import com.phenikaa.submissionservice.util.CloudinaryPublicIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

@Component("cloudinaryFileService")
@RequiredArgsConstructor
@Slf4j
public class CloudinaryFileAdapter implements FileStorageService {
    
    private final CloudinaryService cloudinaryService;
    
    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        log.info("CloudinaryFileAdapter: Adapting CloudinaryService.uploadFile to FileStorageService.uploadFile");
        return cloudinaryService.uploadFile(file, folderName);
    }
    
    @Override
    public byte[] downloadFile(String filePath) {
        log.info("CloudinaryFileAdapter: Adapting CloudinaryService.downloadFile to FileStorageService.downloadFile");
        String publicId = CloudinaryPublicIdUtils.extractPublicId(filePath);
        return cloudinaryService.downloadFile(publicId);
    }
    
    @Override
    public void deleteFile(String filePath) {
        log.info("CloudinaryFileAdapter: Adapting CloudinaryService.deleteFile to FileStorageService.deleteFile");
        String publicId = CloudinaryPublicIdUtils.extractPublicId(filePath);
        cloudinaryService.deleteFile(publicId);
    }
    
    @Override
    public String generateFileUrl(String filePath) {
        log.info("CloudinaryFileAdapter: Adapting CloudinaryService.generateSignedUrl to FileStorageService.generateFileUrl");
        String publicId = CloudinaryPublicIdUtils.extractPublicId(filePath);
        return cloudinaryService.generateSignedUrl(publicId);
    }
    
    @Override
    public void streamFile(String filePath, OutputStream outputStream) {
        log.info("CloudinaryFileAdapter: Adapting CloudinaryService streaming to FileStorageService.streamFile");
        try {
            String publicId = CloudinaryPublicIdUtils.extractPublicId(filePath);
            byte[] data = cloudinaryService.downloadFile(publicId);
            outputStream.write(data);
        } catch (Exception e) {
            log.error("CloudinaryFileAdapter: Error streaming file", e);
            throw new RuntimeException("Stream failed", e);
        }
    }
}
