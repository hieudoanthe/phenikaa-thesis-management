package com.phenikaa.submissionservice.controller;

import com.phenikaa.submissionservice.dto.request.ReportSubmissionRequest;
import com.phenikaa.submissionservice.dto.response.ReportSubmissionResponse;
import com.phenikaa.submissionservice.service.ReportSubmissionService;
import com.phenikaa.submissionservice.service.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/api/submission-service")
@RequiredArgsConstructor
@Slf4j
public class ReportSubmissionController {

    private final ReportSubmissionService reportSubmissionService;
    private final CloudinaryService cloudinaryService;

    /**
     * Tạo báo cáo mới
     */
    @PostMapping(value = "/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportSubmissionResponse> createSubmission(
            @Valid @ModelAttribute ReportSubmissionRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            log.info("Creating new report submission for topic: {}", request.getTopicId());
            ReportSubmissionResponse response = reportSubmissionService.createSubmission(request, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cập nhật báo cáo
     */
    @PutMapping(value = "/submissions/{submissionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportSubmissionResponse> updateSubmission(
            @PathVariable Integer submissionId,
            @Valid @ModelAttribute ReportSubmissionRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            log.info("Updating submission: {}", submissionId);
            ReportSubmissionResponse response = reportSubmissionService.updateSubmission(submissionId, request, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy báo cáo theo ID
     */
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ReportSubmissionResponse> getSubmissionById(@PathVariable Integer submissionId) {
        try {
            ReportSubmissionResponse response = reportSubmissionService.getSubmissionById(submissionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Lấy báo cáo theo topic
     */
    @GetMapping("/submissions/topic/{topicId}")
    public ResponseEntity<List<ReportSubmissionResponse>> getSubmissionsByTopic(@PathVariable Integer topicId) {
        try {
            List<ReportSubmissionResponse> responses = reportSubmissionService.getSubmissionsByTopic(topicId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting submissions by topic: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy báo cáo theo người dùng
     */
    @GetMapping("/submissions/user/{userId}")
    public ResponseEntity<List<ReportSubmissionResponse>> getSubmissionsByUser(@PathVariable Integer userId) {
        try {
            List<ReportSubmissionResponse> responses = reportSubmissionService.getSubmissionsByUser(userId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting submissions by user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy báo cáo với phân trang
     */
    @GetMapping("/submissions")
    public ResponseEntity<Page<ReportSubmissionResponse>> getSubmissionsWithPagination(Pageable pageable) {
        try {
            Page<ReportSubmissionResponse> responses = reportSubmissionService.getSubmissionsWithPagination(pageable);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting submissions with pagination: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cập nhật trạng thái báo cáo
     */
    @PutMapping("/submissions/{submissionId}/status")
    public ResponseEntity<ReportSubmissionResponse> updateSubmissionStatus(
            @PathVariable Integer submissionId,
            @RequestParam Integer status) {
        try {
            log.info("Updating submission status: {} to {}", submissionId, status);
            ReportSubmissionResponse response = reportSubmissionService.updateSubmissionStatus(submissionId, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating submission status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy URL file để download
     */
    @GetMapping("/submissions/{submissionId}/file-url")
    public ResponseEntity<String> getFileUrl(@PathVariable Integer submissionId) {
        try {
            ReportSubmissionResponse submission = reportSubmissionService.getSubmissionById(submissionId);
            if (submission.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(submission.getFilePath());
        } catch (Exception e) {
            log.error("Error getting file URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy file với proper headers để hiển thị đúng loại file
     */
    @GetMapping("/submissions/{submissionId}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Integer submissionId) {
        try {
            ReportSubmissionResponse submission = reportSubmissionService.getSubmissionById(submissionId);
            if (submission.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("Getting file for submission {} with URL: {}", submissionId, submission.getFilePath());

            // Thử download trực tiếp từ URL trước
            try {
                // Sử dụng HTTP client thay vì URL.openStream() để có better error handling
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(submission.getFilePath()))
                    .GET()
                    .build();
                
                java.net.http.HttpResponse<byte[]> response = client.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                
                if (response.statusCode() == 200) {
                    byte[] fileContent = response.body();
                    
                    // Lấy file extension từ URL
                    String fileExtension = getFileExtensionFromUrl(submission.getFilePath());
                    MediaType mediaType = getMediaTypeFromExtension(fileExtension);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(mediaType);
                    headers.setContentDispositionFormData("inline", "report." + fileExtension);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(fileContent);
                } else {
                    throw new RuntimeException("HTTP error: " + response.statusCode());
                }
            } catch (Exception directDownloadError) {
                log.warn("Direct download failed, trying with Cloudinary SDK: {}", directDownloadError.getMessage());
                
                // Fallback: sử dụng Cloudinary SDK
                String publicId = extractPublicIdFromUrl(submission.getFilePath());
                if (publicId == null) {
                    log.error("Cannot extract public_id from URL: {}", submission.getFilePath());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                try {
                    // Lấy file extension từ URL gốc
                    String fileExtension = getFileExtensionFromUrl(submission.getFilePath());
                    log.info("File extension: {}", fileExtension);
                    
                    // Thử download trực tiếp từ Cloudinary API
                    byte[] fileContent = cloudinaryService.downloadFileDirectly(publicId);

                    MediaType mediaType = getMediaTypeFromExtension(fileExtension);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(mediaType);
                    headers.setContentDispositionFormData("inline", "report." + fileExtension);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(fileContent);
                } catch (Exception cloudinaryError) {
                    log.error("Cloudinary direct download also failed: {}", cloudinaryError.getMessage());
                    
                    // Final fallback: return error with helpful message
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(("Không thể tải file. Vui lòng thử lại sau.").getBytes());
                }
            }
        } catch (Exception e) {
            log.error("Error getting file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Xóa báo cáo
     */
    @DeleteMapping("/submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Integer submissionId) {
        try {
            log.info("Deleting submission: {}", submissionId);
            reportSubmissionService.deleteSubmission(submissionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper method để lấy file extension từ URL
    private String getFileExtensionFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "bin";
        }

        int lastDotIndex = fileUrl.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileUrl.length() - 1) {
            return fileUrl.substring(lastDotIndex + 1).toLowerCase();
        }

        return "bin";
    }

    // Helper method để map file extension thành MediaType
    private MediaType getMediaTypeFromExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "doc":
            case "docx":
                return MediaType.valueOf("application/msword");
            case "xls":
            case "xlsx":
                return MediaType.valueOf("application/vnd.ms-excel");
            case "ppt":
            case "pptx":
                return MediaType.valueOf("application/vnd.ms-powerpoint");
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "zip":
                return MediaType.valueOf("application/zip");
            case "rar":
                return MediaType.valueOf("application/x-rar-compressed");
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    // Helper method để extract public_id từ Cloudinary URL
    private String extractPublicIdFromUrl(String fileUrl) {
        try {
            log.info("Extracting public_id from URL: {}", fileUrl);
            
            if (fileUrl == null || !fileUrl.contains("cloudinary.com")) {
                log.warn("URL is null or not a Cloudinary URL");
                return null;
            }
            
            // Extract the part after /upload/ and before the file extension
            String[] parts = fileUrl.split("/upload/");
            if (parts.length > 1) {
                String pathWithVersion = parts[1];
                log.info("Path with version: {}", pathWithVersion);
                
                // Remove version prefix (v1234567890/)
                String[] pathParts = pathWithVersion.split("/", 2);
                if (pathParts.length > 1) {
                    String publicId = pathParts[1].split("\\.")[0]; // Remove file extension
                    log.info("Extracted public_id: {}", publicId);
                    return publicId;
                }
            }
            log.warn("Could not extract public_id from URL");
            return null;
        } catch (Exception e) {
            log.error("Error extracting public_id from URL: {}", e.getMessage(), e);
            return null;
        }
    }
}