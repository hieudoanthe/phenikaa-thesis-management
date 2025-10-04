package com.phenikaa.submissionservice.controller;

import com.phenikaa.submissionservice.dto.request.ReportSubmissionRequest;
import com.phenikaa.submissionservice.dto.request.SubmissionFilterRequest;
import com.phenikaa.submissionservice.dto.response.ReportSubmissionResponse;
import com.phenikaa.submissionservice.dto.response.SubmissionStatusResponse;
import com.phenikaa.submissionservice.service.ReportSubmissionService;
import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@RestController
@RequestMapping("/api/submission-service")
@Slf4j
public class ReportSubmissionController {

    private final ReportSubmissionService reportSubmissionService;
    private final FileStorageService fileStorageService;

    public ReportSubmissionController(
            ReportSubmissionService reportSubmissionService,
            @org.springframework.beans.factory.annotation.Qualifier("cloudinaryFileService") FileStorageService fileStorageService
    ) {
        this.reportSubmissionService = reportSubmissionService;
        this.fileStorageService = fileStorageService;
    }

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

    @PostMapping("/submissions/filter")
    public ResponseEntity<Page<ReportSubmissionResponse>> filterSubmissions(
            @RequestBody SubmissionFilterRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Integer studentId
    ) {
        try {
            // Nếu không truyền header, có thể lấy từ SecurityContext sau này
            Page<ReportSubmissionResponse> responses = reportSubmissionService.filterSubmissions(request, studentId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error filtering submissions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(submission.getFilePath()))
                    .GET()
                    .build();
                
                HttpResponse<byte[]> response = client.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());
                
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
                log.warn("Direct download failed, trying with storage service: {}", directDownloadError.getMessage());
                try {
                    String fileExtension = getFileExtensionFromUrl(submission.getFilePath());
                    byte[] fileContent = fileStorageService.downloadFile(submission.getFilePath());

                    MediaType mediaType = getMediaTypeFromExtension(fileExtension);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(mediaType);
                    headers.setContentDispositionFormData("inline", "report." + fileExtension);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(fileContent);
                } catch (Exception storageError) {
                    log.error("Storage service download also failed: {}", storageError.getMessage());
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(("Không thể tải file. Vui lòng thử lại sau.").getBytes());
                }
            }
        } catch (Exception e) {
            log.error("Error getting file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    @GetMapping("/submissions/status/{userId}")
    public ResponseEntity<SubmissionStatusResponse> getSubmissionStatus(@PathVariable Integer userId) {
        try {
            log.info("Getting submission status for user: {}", userId);
            SubmissionStatusResponse response = reportSubmissionService.checkSubmissionStatus(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting submission status for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}