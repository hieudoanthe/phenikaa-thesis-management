package com.phenikaa.submissionservice.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phenikaa.submissionservice.service.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;
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

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return String.valueOf(uploadResult.get("secure_url"));
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
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = (Map<String, Object>) (Map<?, ?>) cloudinary.api().resource(publicId, options);
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

    // Adaptee remains focused on Cloudinary only

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