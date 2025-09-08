package com.phenikaa.submissionservice.controller;

import com.phenikaa.submissionservice.service.FileStorageStrategy;
import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/submission-service/files")
@RequiredArgsConstructor
@Slf4j
public class FileStorageController {
    
    private final FileStorageStrategy fileStorageStrategy;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @Qualifier("traditionalFileService")
    private FileStorageService traditionalService;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @Qualifier("nioFileService")
    private FileStorageService nioService;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @Qualifier("cloudinaryFileService")
    private FileStorageService cloudinaryService;
    
    // Dynamic file operations based on configuration
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("folder") String folder) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            FileStorageService service = fileStorageStrategy.getFileStorageService();
            String filePath = service.uploadFile(file, folder);
            
            response.put("status", "success");
            response.put("filePath", filePath);
            response.put("storageType", fileStorageStrategy.getCurrentStorageType());
            response.put("message", "File uploaded successfully using " + fileStorageStrategy.getCurrentStorageType());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("File upload failed", e);
            response.put("status", "error");
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String path) {
        try {
            FileStorageService service = fileStorageStrategy.getFileStorageService();
            byte[] fileContent = service.downloadFile(path);
            
            // Determine content type
            String contentType = Files.probeContentType(Paths.get(path));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", Paths.get(path).getFileName().toString());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
        } catch (Exception e) {
            log.error("File download failed", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // Traditional I/O endpoints
    @PostMapping("/traditional/upload")
    public ResponseEntity<Map<String, Object>> uploadTraditional(@RequestParam("file") MultipartFile file,
                                                                @RequestParam("folder") String folder) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (traditionalService == null) {
                response.put("status", "error");
                response.put("message", "Traditional I/O service not available");
                return ResponseEntity.badRequest().body(response);
            }
            
            String filePath = traditionalService.uploadFile(file, folder);
            
            response.put("status", "success");
            response.put("filePath", filePath);
            response.put("storageType", "traditional");
            response.put("message", "File uploaded using Traditional I/O");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Traditional I/O upload failed", e);
            response.put("status", "error");
            response.put("message", "Traditional upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/traditional/download")
    public ResponseEntity<byte[]> downloadTraditional(@RequestParam String path) {
        try {
            if (traditionalService == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = traditionalService.downloadFile(path);
            
            String contentType = Files.probeContentType(Paths.get(path));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", Paths.get(path).getFileName().toString());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
        } catch (Exception e) {
            log.error("Traditional I/O download failed", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // Java NIO endpoints
    @PostMapping("/nio/upload")
    public ResponseEntity<Map<String, Object>> uploadNIO(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("folder") String folder) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (nioService == null) {
                response.put("status", "error");
                response.put("message", "Java NIO service not available");
                return ResponseEntity.badRequest().body(response);
            }
            
            String filePath = nioService.uploadFile(file, folder);
            
            response.put("status", "success");
            response.put("filePath", filePath);
            response.put("storageType", "nio");
            response.put("message", "File uploaded using Java NIO");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Java NIO upload failed", e);
            response.put("status", "error");
            response.put("message", "NIO upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/nio/download")
    public ResponseEntity<byte[]> downloadNIO(@RequestParam String path) {
        try {
            if (nioService == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = nioService.downloadFile(path);
            
            String contentType = Files.probeContentType(Paths.get(path));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", Paths.get(path).getFileName().toString());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
        } catch (Exception e) {
            log.error("Java NIO download failed", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // Cloudinary endpoints
    @PostMapping("/cloudinary/upload")
    public ResponseEntity<Map<String, Object>> uploadCloudinary(@RequestParam("file") MultipartFile file,
                                                               @RequestParam("folder") String folder) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (cloudinaryService == null) {
                response.put("status", "error");
                response.put("message", "Cloudinary service not available");
                return ResponseEntity.badRequest().body(response);
            }
            
            String filePath = cloudinaryService.uploadFile(file, folder);
            
            response.put("status", "success");
            response.put("filePath", filePath);
            response.put("storageType", "cloudinary");
            response.put("message", "File uploaded using Cloudinary");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Cloudinary upload failed", e);
            response.put("status", "error");
            response.put("message", "Cloudinary upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Performance comparison endpoint
    @PostMapping("/compare-performance")
    public ResponseEntity<Map<String, Object>> comparePerformance(@RequestParam("file") MultipartFile file,
                                                                 @RequestParam("folder") String folder) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            fileStorageStrategy.comparePerformance(file, folder);
            response.put("status", "success");
            response.put("message", "Performance comparison completed. Check logs for details.");
        } catch (Exception e) {
            log.error("Performance comparison failed", e);
            response.put("status", "error");
            response.put("message", "Performance comparison failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // Get current storage configuration
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("currentStorageType", fileStorageStrategy.getCurrentStorageType());
        config.put("traditionalAvailable", traditionalService != null);
        config.put("nioAvailable", nioService != null);
        config.put("cloudinaryAvailable", cloudinaryService != null);
        
        return ResponseEntity.ok(config);
    }
    
    // File streaming endpoint
    @GetMapping("/stream")
    public ResponseEntity<byte[]> streamFile(@RequestParam String path) {
        try {
            FileStorageService service = fileStorageStrategy.getFileStorageService();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            service.streamFile(path, baos);
            byte[] fileContent = baos.toByteArray();
            
            String contentType = Files.probeContentType(Paths.get(path));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("inline", Paths.get(path).getFileName().toString());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
        } catch (Exception e) {
            log.error("File streaming failed", e);
            return ResponseEntity.notFound().build();
        }
    }
}
