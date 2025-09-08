package com.phenikaa.submissionservice.service;

import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileStorageStrategy {
    
    @Value("${file.storage.type:cloudinary}") // traditional, nio, cloudinary
    private String storageType;
    
    @Autowired
    @Qualifier("traditionalFileService")
    private FileStorageService traditionalService;
    
    @Autowired
    @Qualifier("nioFileService")
    private FileStorageService nioService;
    
    @Autowired
    @Qualifier("cloudinaryFileService")
    private FileStorageService cloudinaryService;
    
    public FileStorageService getFileStorageService() {
        return switch (storageType.toLowerCase()) {
            case "traditional" -> {
                log.info("Using Traditional I/O for file operations");
                yield traditionalService;
            }
            case "nio" -> {
                log.info("Using Java NIO for file operations");
                yield nioService;
            }
            case "cloudinary" -> {
                log.info("Using Cloudinary for file operations");
                yield cloudinaryService;
            }
            default -> {
                log.warn("Unknown storage type: {}, defaulting to Cloudinary", storageType);
                yield cloudinaryService;
            }
        };
    }
    
    // Method để so sánh performance
    public void comparePerformance(MultipartFile file, String folderName) {
        long startTime, endTime;
        
        try {
            // Test Traditional I/O
            startTime = System.currentTimeMillis();
            String traditionalPath = traditionalService.uploadFile(file, folderName + "/traditional");
            endTime = System.currentTimeMillis();
            log.info("Traditional I/O upload time: {} ms", endTime - startTime);
            
            // Clean up
            traditionalService.deleteFile(traditionalPath);
            
        } catch (Exception e) {
            log.error("Traditional I/O performance test failed", e);
        }
        
        try {
            // Test Java NIO
            startTime = System.currentTimeMillis();
            String nioPath = nioService.uploadFile(file, folderName + "/nio");
            endTime = System.currentTimeMillis();
            log.info("Java NIO upload time: {} ms", endTime - startTime);
            
            // Clean up
            nioService.deleteFile(nioPath);
            
        } catch (Exception e) {
            log.error("Java NIO performance test failed", e);
        }
        
        try {
            // Test Cloudinary
            startTime = System.currentTimeMillis();
            String cloudinaryPath = cloudinaryService.uploadFile(file, folderName + "/cloudinary");
            endTime = System.currentTimeMillis();
            log.info("Cloudinary upload time: {} ms", endTime - startTime);
            
            // Clean up
            cloudinaryService.deleteFile(cloudinaryPath);
            
        } catch (Exception e) {
            log.error("Cloudinary performance test failed", e);
        }
    }
    
    // Method để lấy thông tin storage hiện tại
    public String getCurrentStorageType() {
        return storageType;
    }
    
    // Method để kiểm tra storage availability
    public boolean isStorageAvailable(String storageType) {
        return switch (storageType.toLowerCase()) {
            case "traditional", "nio", "cloudinary" -> true;
            default -> false;
        };
    }
}
