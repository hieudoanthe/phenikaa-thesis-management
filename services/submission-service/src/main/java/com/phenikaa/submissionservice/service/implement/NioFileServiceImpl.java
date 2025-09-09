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
import java.util.List;
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
    
    // Java NIO - Text file processing
    public void processTextFileNIO(String inputPath, String outputPath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(inputPath));
            
            List<String> processedLines = lines.stream()
                .map(this::processLine)
                .toList();
            
            Files.write(Paths.get(outputPath), processedLines, StandardOpenOption.CREATE);
            log.info("Text file processed using Java NIO");
        } catch (IOException e) {
            log.error("Java NIO text processing failed", e);
            throw new RuntimeException("NIO text processing failed", e);
        }
    }
    
    // Java NIO - Binary file processing
    public void processBinaryFileNIO(String inputPath, String outputPath) {
        try (FileChannel inputChannel = FileChannel.open(Paths.get(inputPath));
             FileChannel outputChannel = FileChannel.open(Paths.get(outputPath), 
                 StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                
                // Process binary data
                processBinaryBuffer(buffer);
                
                outputChannel.write(buffer);
                buffer.clear();
            }
            log.info("Binary file processed using Java NIO");
        } catch (IOException e) {
            log.error("Java NIO binary processing failed", e);
            throw new RuntimeException("NIO binary processing failed", e);
        }
    }
    
    // Java NIO - File watching
    public void watchFileChanges(String directoryPath) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(directoryPath);
            path.register(watchService, 
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
            
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();
                    
                    log.info("File {} was {}", fileName, kind);
                }
                key.reset();
            }
        } catch (IOException e) {
            log.error("Java NIO file watching failed", e);
            throw new RuntimeException("NIO file watching failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Java NIO file watching interrupted", e);
            throw new RuntimeException("NIO file watching interrupted", e);
        }
    }
    
    private String processLine(String line) {
        // Simple processing example
        return line.toUpperCase();
    }
    
    private void processBinaryBuffer(ByteBuffer buffer) {
        // Simple binary processing example
        for (int i = 0; i < buffer.limit(); i++) {
            buffer.put(i, (byte) (buffer.get(i) ^ 0xFF)); // Simple XOR
        }
    }
    
    private String getFileExtension(String filename) {
        return filename != null && filename.contains(".") 
            ? filename.substring(filename.lastIndexOf(".")) 
            : "";
    }
}
