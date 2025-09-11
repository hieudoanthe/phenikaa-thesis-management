package com.phenikaa.submissionservice.service.implement;

import com.phenikaa.submissionservice.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;

@Service("nioFileService")
@Slf4j
public class NioFileServiceImpl implements FileStorageService {
    
    private final Path basePath = Paths.get("uploads");
    
    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        try {
            // Tạo thư mục với NIO
            Path uploadPath = basePath.resolve(folderName);
            Files.createDirectories(uploadPath);
            
            String fileName = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(fileName);
            
            // Java NIO - Files.copy()
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            log.info("File uploaded using Java NIO: {}", filePath);
            return filePath.toString();
            
        } catch (IOException e) {
            log.error("Java NIO upload failed", e);
            throw new RuntimeException("NIO upload failed", e);
        }
    }
    
    @Override
    public byte[] downloadFile(String filePath) {
        try {
            // Java NIO - Files.readAllBytes()
            byte[] data = Files.readAllBytes(Paths.get(filePath));
            log.info("File downloaded using Java NIO: {}", filePath);
            return data;
        } catch (IOException e) {
            log.error("Java NIO download failed", e);
            throw new RuntimeException("NIO download failed", e);
        }
    }
    
    @Override
    public void deleteFile(String filePath) {
        try {
            // Java NIO - Files.deleteIfExists()
            Files.deleteIfExists(Paths.get(filePath));
            log.info("File deleted using Java NIO: {}", filePath);
        } catch (IOException e) {
            log.error("Java NIO delete failed", e);
            throw new RuntimeException("NIO delete failed", e);
        }
    }
    
    @Override
    public String generateFileUrl(String filePath) {
        return "/api/files/nio/download?path=" + URLEncoder.encode(filePath, StandardCharsets.UTF_8);
    }
    
    @Override
    public void streamFile(String filePath, OutputStream outputStream) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath))) {
            // Java NIO - FileChannel streaming
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            
            while (fileChannel.read(buffer) != -1) {
                buffer.flip();
                outputStream.write(buffer.array(), 0, buffer.limit());
                buffer.clear();
            }
            log.info("File streamed using Java NIO: {}", filePath);
        } catch (IOException e) {
            log.error("Java NIO streaming failed", e);
            throw new RuntimeException("NIO streaming failed", e);
        }
    }

    private String getFileExtension(String filename) {
        return filename != null && filename.contains(".") 
            ? filename.substring(filename.lastIndexOf(".")) 
            : "";
    }
}
