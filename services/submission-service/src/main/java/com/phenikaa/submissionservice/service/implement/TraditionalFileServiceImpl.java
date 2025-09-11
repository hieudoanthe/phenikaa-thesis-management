package com.phenikaa.submissionservice.service.implement;

import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service("traditionalFileService")
@Slf4j
public class TraditionalFileServiceImpl implements FileStorageService {
    
    private final String basePath = "uploads";
    
    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        try {
            // Tạo thư mục với Traditional I/O
            File uploadDir = new File(basePath + File.separator + folderName);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            String fileName = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
            File targetFile = new File(uploadDir, fileName);

            // Traditional I/O - FileInputStream/FileOutputStream
            try (InputStream inputStream = file.getInputStream();
                 FileOutputStream fos = new FileOutputStream(targetFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            log.info("File uploaded using Traditional I/O: {}", targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath();
            
        } catch (IOException e) {
            log.error("Traditional I/O upload failed", e);
            throw new RuntimeException("Traditional upload failed", e);
        }
    }

    @Override
    public byte[] downloadFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Traditional I/O - FileInputStream
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            log.info("File downloaded using Traditional I/O: {}", filePath);
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Traditional I/O download failed", e);
            throw new RuntimeException("Traditional download failed", e);
        }
    }
    
    @Override
    public void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                log.info("File deleted using Traditional I/O: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Traditional I/O delete failed", e);
            throw new RuntimeException("Traditional delete failed", e);
        }
    }
    
    @Override
    public String generateFileUrl(String filePath) {
        return "/api/files/traditional/download?path=" + URLEncoder.encode(filePath, StandardCharsets.UTF_8);
    }
    
    @Override
    public void streamFile(String filePath, OutputStream outputStream) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            // Traditional I/O streaming
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            log.info("File streamed using Traditional I/O: {}", filePath);
        } catch (IOException e) {
            log.error("Traditional I/O streaming failed", e);
            throw new RuntimeException("Traditional streaming failed", e);
        }
    }
    
    private String getFileExtension(String filename) {
        return filename != null && filename.contains(".") 
            ? filename.substring(filename.lastIndexOf(".")) 
            : "";
    }
}
