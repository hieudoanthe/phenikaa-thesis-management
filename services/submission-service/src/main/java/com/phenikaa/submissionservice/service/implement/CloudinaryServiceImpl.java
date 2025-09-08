package com.phenikaa.submissionservice.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phenikaa.submissionservice.service.interfaces.CloudinaryService;
import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service("cloudinaryFileService")
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService, FileStorageService {
    private final Cloudinary cloudinary;
    
    @Value("${file.storage.cloudinary.backup-local:false}")
    private boolean backupLocal;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @Qualifier("traditionalFileService")
    private FileStorageService traditionalService;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @Qualifier("nioFileService")
    private FileStorageService nioService;

    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        try {
            // Lấy extension của file
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);

            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "raw", // Cho phép upload bất kỳ loại file nào
                    "format", fileExtension, // Sử dụng extension gốc của file
                    "transformation", "f_auto" // Tự động detect format
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Delete failed", e);
        }
    }

    @Override
    public byte[] downloadFile(String publicId) {
        try {
            // Tạo signed URL với authentication
            String downloadUrl = cloudinary.url()
                .resourceType("raw")
                .secure(true)
                .generate(publicId);
            
            log.info("Generated download URL: {}", downloadUrl);
            
            // Sử dụng HTTP client để download với proper headers
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(downloadUrl))
                .GET()
                .build();
            
            java.net.http.HttpResponse<byte[]> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.error("HTTP error: {} for URL: {}", response.statusCode(), downloadUrl);
                throw new RuntimeException("HTTP error: " + response.statusCode());
            }
        } catch (Exception e) {
            log.error("Error downloading file with publicId: {}, error: {}", publicId, e.getMessage(), e);
            throw new RuntimeException("Download failed", e);
        }
    }

    /**
     * Tạo signed URL cho file
     */
    public String generateSignedUrl(String publicId) {
        try {
            return cloudinary.url()
                .resourceType("raw")
                .secure(true)
                .format("auto") // Tự động detect format
                .generate(publicId);
        } catch (Exception e) {
            log.error("Error generating signed URL for publicId: {}, error: {}", publicId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate signed URL", e);
        }
    }

    /**
     * Tạo signed URL với file extension cụ thể
     */
    public String generateSignedUrlWithExtension(String publicId, String fileExtension) {
        try {
            return cloudinary.url()
                .resourceType("raw")
                .secure(true)
                .format(fileExtension) // Sử dụng extension cụ thể
                .generate(publicId);
        } catch (Exception e) {
            log.error("Error generating signed URL with extension for publicId: {}, extension: {}, error: {}", 
                publicId, fileExtension, e.getMessage(), e);
            throw new RuntimeException("Failed to generate signed URL with extension", e);
        }
    }

    /**
     * Download file trực tiếp từ Cloudinary API (không qua URL)
     */
    public byte[] downloadFileDirectly(String publicId) {
        try {
            // Sử dụng Cloudinary API để lấy file trực tiếp
            Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw"
            );
            
            // Lấy thông tin file từ Cloudinary
            Map<String, Object> resource = cloudinary.api().resource(publicId, options);
            String secureUrl = (String) resource.get("secure_url");
            
            if (secureUrl == null) {
                throw new RuntimeException("Cannot get secure URL for publicId: " + publicId);
            }
            
            log.info("Downloading file directly from: {}", secureUrl);
            
            // Download file từ secure URL
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(secureUrl))
                .GET()
                .build();
            
            java.net.http.HttpResponse<byte[]> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new RuntimeException("HTTP error: " + response.statusCode());
            }
        } catch (Exception e) {
            log.error("Error downloading file directly with publicId: {}, error: {}", publicId, e.getMessage(), e);
            throw new RuntimeException("Download failed", e);
        }
    }

    // FileStorageService implementation methods
    @Override
    public String generateFileUrl(String filePath) {
        // Extract public_id from Cloudinary URL
        String publicId = extractPublicIdFromUrl(filePath);
        if (publicId != null) {
            return generateSignedUrl(publicId);
        }
        return filePath; // Return original path if not a Cloudinary URL
    }
    
    @Override
    public void streamFile(String filePath, OutputStream outputStream) {
        try {
            byte[] fileContent = downloadFile(extractPublicIdFromUrl(filePath));
            outputStream.write(fileContent);
        } catch (Exception e) {
            log.error("Error streaming file: {}", e.getMessage(), e);
            throw new RuntimeException("Stream failed", e);
        }
    }
    
    // Hybrid upload method - upload to Cloudinary and optionally backup locally
    public String uploadFileHybrid(MultipartFile file, String folderName) {
        // Upload to Cloudinary first
        String cloudinaryUrl = uploadFile(file, folderName);
        
        // Backup to local storage if enabled
        if (backupLocal) {
            try {
                if (nioService != null) {
                    String localPath = nioService.uploadFile(file, folderName + "/backup");
                    log.info("File backed up locally using NIO: {}", localPath);
                } else if (traditionalService != null) {
                    String localPath = traditionalService.uploadFile(file, folderName + "/backup");
                    log.info("File backed up locally using Traditional I/O: {}", localPath);
                }
            } catch (Exception e) {
                log.warn("Local backup failed: {}", e.getMessage());
            }
        }
        
        return cloudinaryUrl;
    }
    
    // Fallback download method - try Cloudinary first, then local
    public byte[] downloadFileWithFallback(String fileUrl) {
        try {
            // Try Cloudinary first
            String publicId = extractPublicIdFromUrl(fileUrl);
            if (publicId != null) {
                return downloadFile(publicId);
            }
        } catch (Exception e) {
            log.warn("Cloudinary download failed, trying local: {}", e.getMessage());
        }
        
        // Fallback to local storage
        try {
            if (nioService != null) {
                return nioService.downloadFile(fileUrl);
            } else if (traditionalService != null) {
                return traditionalService.downloadFile(fileUrl);
            }
        } catch (Exception e) {
            log.error("Local download also failed: {}", e.getMessage());
        }
        
        throw new RuntimeException("All download methods failed");
    }
    
    // Extract public_id from Cloudinary URL
    private String extractPublicIdFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Extract public_id from URL like: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/filename.ext
            String[] parts = fileUrl.split("/upload/");
            if (parts.length > 1) {
                String pathPart = parts[1];
                // Remove version if present
                if (pathPart.startsWith("v")) {
                    int slashIndex = pathPart.indexOf("/");
                    if (slashIndex > 0) {
                        pathPart = pathPart.substring(slashIndex + 1);
                    }
                }
                // Remove file extension
                int dotIndex = pathPart.lastIndexOf(".");
                if (dotIndex > 0) {
                    pathPart = pathPart.substring(0, dotIndex);
                }
                return pathPart;
            }
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", fileUrl);
        }
        
        return null;
    }

    // Helper method để lấy file extension
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "bin"; // Default extension
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }

        return "bin"; // Default extension nếu không tìm thấy
    }
}