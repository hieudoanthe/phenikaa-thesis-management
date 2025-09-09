package com.phenikaa.submissionservice.service;

import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class FileStorageStrategy {
    
    @Value("${file.storage.type:cloudinary}") // cloudinary | nio | traditional | s3 | local (alias of nio)
    private String storageType;

    @Autowired
    private Map<String, FileStorageService> storageRegistry; // bean name -> service
    
    public FileStorageService getFileStorageService() {
        String key = normalizeKey(storageType);
        FileStorageService service = storageRegistry.get(key);
        if (service != null) {
            log.info("Using {} for file operations (bean: {})", storageType, key);
            return service;
        }

        // Fallback order: cloudinary -> nio -> traditional (first available)
        FileStorageService fallback = Optional
                .ofNullable(storageRegistry.get("cloudinaryFileService"))
                .orElseGet(() -> Optional.ofNullable(storageRegistry.get("nioFileService"))
                        .orElse(storageRegistry.get("traditionalFileService")));

        if (fallback != null) {
            log.warn("Unknown or unavailable storage type: {}. Falling back to {}",
                    storageType,
                    fallback.getClass().getSimpleName());
            return fallback;
        }

        throw new IllegalStateException("No FileStorageService beans available (expected one of: cloudinaryFileService, nioFileService, traditionalFileService)");
    }

    // Fallback operations exposed to clients that want policy-based resilience
    public String uploadWithFallback(MultipartFile file, String folderName) {
        try {
            return getFileStorageService().uploadFile(file, folderName);
        } catch (Exception primaryError) {
            log.warn("Primary storage upload failed: {}. Trying fallback...", primaryError.getMessage());
            FileStorageService fallback = fallbackChain();
            if (fallback == null) throw primaryError;
            return fallback.uploadFile(file, folderName);
        }
    }

    public byte[] downloadWithFallback(String filePath) {
        try {
            return getFileStorageService().downloadFile(filePath);
        } catch (Exception primaryError) {
            log.warn("Primary storage download failed: {}. Trying fallback...", primaryError.getMessage());
            FileStorageService fallback = fallbackChain();
            if (fallback == null) throw primaryError;
            return fallback.downloadFile(filePath);
        }
    }

    public void deleteWithFallback(String filePath) {
        try {
            getFileStorageService().deleteFile(filePath);
        } catch (Exception primaryError) {
            log.warn("Primary storage delete failed: {}. Trying fallback...", primaryError.getMessage());
            FileStorageService fallback = fallbackChain();
            if (fallback == null) throw primaryError;
            fallback.deleteFile(filePath);
        }
    }

    public String generateUrlWithFallback(String filePath) {
        try {
            return getFileStorageService().generateFileUrl(filePath);
        } catch (Exception primaryError) {
            log.warn("Primary storage url generation failed: {}. Trying fallback...", primaryError.getMessage());
            FileStorageService fallback = fallbackChain();
            if (fallback == null) throw primaryError;
            return fallback.generateFileUrl(filePath);
        }
    }

    private FileStorageService fallbackChain() {
        // Return the first available service that is different from the current selection
        String preferred = normalizeKey(storageType);
        FileStorageService cloud = storageRegistry.get("cloudinaryFileService");
        FileStorageService nio = storageRegistry.get("nioFileService");
        FileStorageService trad = storageRegistry.get("traditionalFileService");

        if (!"cloudinaryFileService".equals(preferred) && cloud != null) return cloud;
        if (!"nioFileService".equals(preferred) && nio != null) return nio;
        if (!"traditionalFileService".equals(preferred) && trad != null) return trad;
        return null;
    }

    private String normalizeKey(String type) {
        if (type == null) return "cloudinaryFileService";
        String t = type.trim().toLowerCase();
        return switch (t) {
            case "cloudinary" -> "cloudinaryFileService";
            case "nio", "local" -> "nioFileService";
            case "traditional" -> "traditionalFileService";
            case "s3" -> "s3FileService"; // if provided in the context
            default -> t; // allow direct bean name
        };
    }
    
    // Method để so sánh performance
    public void comparePerformance(MultipartFile file, String folderName) {
        long startTime;
        long endTime;
        
        try {
            // Test Traditional I/O
            startTime = System.currentTimeMillis();
            FileStorageService traditional = storageRegistry.get("traditionalFileService");
            String traditionalPath = traditional.uploadFile(file, folderName + "/traditional");
            endTime = System.currentTimeMillis();
            log.info("Traditional I/O upload time: {} ms", endTime - startTime);
            
            // Clean up
            traditional.deleteFile(traditionalPath);
            
        } catch (Exception e) {
            log.error("Traditional I/O performance test failed", e);
        }
        
        try {
            // Test Java NIO
            startTime = System.currentTimeMillis();
            FileStorageService nioServiceBean = storageRegistry.get("nioFileService");
            String nioPath = nioServiceBean.uploadFile(file, folderName + "/nio");
            endTime = System.currentTimeMillis();
            log.info("Java NIO upload time: {} ms", endTime - startTime);
            
            // Clean up
            nioServiceBean.deleteFile(nioPath);
            
        } catch (Exception e) {
            log.error("Java NIO performance test failed", e);
        }
        
        try {
            // Test Cloudinary
            startTime = System.currentTimeMillis();
            FileStorageService cloudinary = storageRegistry.get("cloudinaryFileService");
            String cloudinaryPath = cloudinary.uploadFile(file, folderName + "/cloudinary");
            endTime = System.currentTimeMillis();
            log.info("Cloudinary upload time: {} ms", endTime - startTime);
            
            // Clean up
            cloudinary.deleteFile(cloudinaryPath);
            
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
