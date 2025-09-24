package com.phenikaa.userservice.service.implement;

import com.phenikaa.userservice.dto.response.ImportResultResponse;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.repository.RoleRepository;
import com.phenikaa.userservice.repository.UserRepository;
import com.phenikaa.userservice.service.interfaces.ImportUserService;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.client.ProfileServiceClient;
import com.phenikaa.dto.request.CreateProfileRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImportUserServiceImpl implements ImportUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileServiceClient profileServiceClient;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ImportResultResponse importStudentsFromCSV(MultipartFile file, Integer periodId, Integer academicYearId) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        List<ImportResultResponse.StudentImportResult> results = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        // Đọc tất cả dữ liệu trước
        List<String[]> csvData = new ArrayList<>();
        int rowNumber = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstRow = true;

            while ((line = reader.readLine()) != null) {
                rowNumber++;
                
                // Bỏ qua header row
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Bỏ qua dòng trống
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] columns = parseCSVLine(line);
                    csvData.add(columns);
                } catch (Exception e) {
                    log.error("Lỗi khi parse dòng {}: {}", rowNumber, e.getMessage());
                    errors.add(new ImportResultResponse.ImportError(
                        rowNumber, "parsing", "Lỗi parse: " + e.getMessage()));
                    errorCount++;
                }
            }

        } catch (Exception e) {
            log.error("Lỗi khi đọc file CSV: {}", e.getMessage(), e);
            return new ImportResultResponse(false, "Lỗi khi đọc file CSV: " + e.getMessage(), 
                0, 0, 1, errors, results);
        }

        // Xử lý batch để tránh connection timeout
        int batchSize = 10; // Tăng batch size lên 10 user mỗi batch
        int totalRows = csvData.size();
        
        log.info("Bắt đầu xử lý {} dòng dữ liệu với batch size {}", totalRows, batchSize);
        
        for (int i = 0; i < csvData.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, csvData.size());
            List<String[]> batch = csvData.subList(i, endIndex);
            
            log.info("Xử lý batch {}-{} / {}", i + 1, endIndex, totalRows);
            
            try {
                BatchResult batchResult = processBatch(batch, i + 1, periodId);
                errors.addAll(batchResult.errors);
                results.addAll(batchResult.results);
                successCount += batchResult.successCount;
                errorCount += batchResult.errorCount;
                
                // Log progress
                log.info("Batch {}-{} hoàn thành: {} thành công, {} lỗi", 
                    i + 1, endIndex, batchResult.successCount, batchResult.errorCount);
                
                // Thêm delay nhỏ giữa các batch để tránh quá tải
                if (i + batchSize < csvData.size()) {
                    Thread.sleep(100); // 100ms delay
                }
                
            } catch (Exception e) {
                log.error("Lỗi khi xử lý batch {}-{}: {}", i + 1, endIndex, e.getMessage(), e);
                // Thêm lỗi cho tất cả dòng trong batch này
                for (int j = i; j < endIndex; j++) {
                    errors.add(new ImportResultResponse.ImportError(
                        j + 1, "batch", "Lỗi batch: " + e.getMessage()));
                    errorCount++;
                }
            }
        }

        String message = String.format("Import hoàn thành: %d thành công, %d lỗi", successCount, errorCount);
        return new ImportResultResponse(true, message, totalRows, successCount, errorCount, errors, results);
    }

    @Override
    public List<Map<String, Object>> getStudentsByPeriod(Integer periodId) {
        try {
            List<User> users = userRepository.findByPeriodId(periodId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (User user : users) {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("userId", user.getUserId());
                studentData.put("fullName", user.getFullName());
                studentData.put("username", user.getUsername()); // Username chính là email
                studentData.put("email", user.getUsername()); // Username = email (để hiển thị)
                studentData.put("createdAt", user.getCreatedAt());
                result.add(studentData);
            }

            return result;
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên theo period: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách sinh viên: " + e.getMessage());
        }
    }

    @Override
    public boolean removeStudentFromPeriod(Integer studentId, Integer periodId) {
        try {
            Optional<User> user = userRepository.findById(studentId);
            if (user.isPresent() && user.get().getPeriodId().equals(periodId)) {
                userRepository.delete(user.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi xóa sinh viên khỏi period: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xóa sinh viên khỏi đợt đăng ký: " + e.getMessage());
        }
    }

    private List<String[]> parseCSV(MultipartFile file) throws Exception {
        List<String[]> csvData = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] columns = parseCSVLine(line);
                csvData.add(columns);
            }
        }
        
        return csvData;
    }

    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private List<String> validateStudentData(String fullName, String username, String password, Integer periodId) {
        List<String> errors = new ArrayList<>();

        if (fullName == null || fullName.trim().isEmpty()) {
            errors.add("Họ tên không được để trống");
        }
        if (username == null || username.trim().isEmpty()) {
            errors.add("Username không được để trống");
        }
        if (password == null || password.trim().isEmpty()) {
            errors.add("Mật khẩu không được để trống");
        }

        // Kiểm tra username đã tồn tại trong cùng đợt (chỉ khi username hợp lệ)
        if (username != null && !username.trim().isEmpty() && userRepository.existsByUsernameAndPeriodId(username, periodId)) {
            errors.add("Username đã tồn tại trong đợt này: " + username);
        }

        return errors;
    }

    /**
     * Xử lý một batch nhỏ để tránh connection timeout
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 300, rollbackFor = Exception.class) // 5 phút timeout
    public BatchResult processBatch(List<String[]> batch, int startRow, Integer periodId) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        List<ImportResultResponse.StudentImportResult> results = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        log.debug("Bắt đầu xử lý batch {} dòng từ dòng {}", batch.size(), startRow);
        
        for (int i = 0; i < batch.size(); i++) {
            String[] columns = batch.get(i);
            int rowNumber = startRow + i;
            
            // Xử lý từng user riêng biệt để tránh rollback issues
            try {
                ImportResultResponse.StudentImportResult result = processSingleUser(columns, rowNumber, periodId);
                if (result != null) {
                    results.add(result);
                    successCount++;
                } else {
                    errorCount++;
                }
            } catch (Exception e) {
                log.error("Lỗi khi xử lý dòng {}: {}", rowNumber, e.getMessage(), e);
                
                // Xử lý các loại lỗi khác nhau
                String errorType = "processing";
                String errorMessage = e.getMessage();
                
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("Connection reset") || 
                        e.getMessage().contains("Connection timed out") ||
                        e.getMessage().contains("AsyncRequestNotUsableException")) {
                        errorType = "connection";
                        errorMessage = "Lỗi kết nối - vui lòng thử lại";
                    } else if (e.getMessage().contains("timeout")) {
                        errorType = "timeout";
                        errorMessage = "Timeout - vui lòng thử lại";
                    } else if (e.getMessage().contains("constraint")) {
                        errorType = "constraint";
                        errorMessage = "Vi phạm ràng buộc dữ liệu";
                    } else if (e.getMessage().contains("rollback")) {
                        errorType = "transaction";
                        errorMessage = "Lỗi transaction - vui lòng thử lại";
                    }
                }
                
                errors.add(new ImportResultResponse.ImportError(
                    rowNumber, errorType, errorMessage));
                errorCount++;
            }
        }
        
        return new BatchResult(errors, results, successCount, errorCount);
    }

    /**
     * Xử lý một user đơn lẻ với transaction riêng biệt
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ImportResultResponse.StudentImportResult processSingleUser(String[] columns, int rowNumber, Integer periodId) {
            try {
                if (columns.length < 3) {
                log.warn("Dòng {} không đủ cột dữ liệu (cần ít nhất 3 cột)", rowNumber);
                return null;
                }

                // Parse dữ liệu từ CSV (format: Họ tên, Username, Password)
                String fullName = columns[0].trim();
                String username = columns[1].trim(); // Username chính là email
                String password = columns[2].trim();

                // Validate dữ liệu
                List<String> validationErrors = validateStudentData(fullName, username, password, periodId);

                if (!validationErrors.isEmpty()) {
                log.warn("Dòng {} có lỗi validation: {}", rowNumber, validationErrors);
                return null;
                }

                // Tạo user mới
                User user = createUser(fullName, username, password, periodId);
                
                // Tạo profile bất đồng bộ để tránh timeout
                createProfileAsync(user.getUserId());

            return new ImportResultResponse.StudentImportResult(
                user.getUserId().toString(), fullName, username, true, "Tạo tài khoản thành công", user.getUserId());

            } catch (Exception e) {
            log.error("Lỗi khi xử lý user ở dòng {}: {}", rowNumber, e.getMessage(), e);
            throw e; // Re-throw để được xử lý ở level cao hơn
        }
    }

    /**
     * Tạo user mới với transaction riêng
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public User createUser(String fullName, String username, String password, Integer periodId) {
        try {
        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username); // Username chính là email
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPeriodId(periodId);
        user.setStatus(1); // Active
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        
        // Set role STUDENT
        Role studentRole = entityManager.find(Role.class, 1);
        if (studentRole != null) {
            user.getRoles().add(studentRole);
        }
        
        entityManager.persist(user);
        entityManager.flush();
        
            log.debug("Tạo user thành công: {}", username);
        return user;
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Không thể tạo user: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo profile bất đồng bộ để tránh timeout
     */
    @Async
    public CompletableFuture<Void> createProfileAsync(Integer userId) {
        return createProfileAsync(userId, "STUDENT");
    }

    /**
     * Tạo profile bất đồng bộ với role cụ thể
     */
    @Async
    public CompletableFuture<Void> createProfileAsync(Integer userId, String role) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                profileServiceClient.createProfile(new CreateProfileRequest(userId, role));
                log.info("Tạo profile thành công cho user: {} với role: {}", userId, role);
                return CompletableFuture.completedFuture(null);
            } catch (Exception profileError) {
                log.warn("Không thể tạo profile cho user {}: {}, retry: {}/{}", 
                    userId, profileError.getMessage(), retryCount + 1, maxRetries);
            }
            
            retryCount++;
            if (retryCount < maxRetries) {
                try {
                    Thread.sleep(1000 * retryCount); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.error("Không thể tạo profile cho user {} sau {} lần thử", userId, maxRetries);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public ImportResultResponse importTeachersFromCSV(MultipartFile file) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        List<ImportResultResponse.StudentImportResult> results = new ArrayList<>();
            int successCount = 0;
            int errorCount = 0;

        // Đọc tất cả dữ liệu trước
        List<String[]> csvData = new ArrayList<>();
        int rowNumber = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstRow = true;

            while ((line = reader.readLine()) != null) {
                rowNumber++;
                
                // Bỏ qua header row
                if (isFirstRow) {
                    isFirstRow = false;
                                continue;
                            }
                            
                // Bỏ qua dòng trống
                if (line.trim().isEmpty()) {
                                continue;
                            }
                            
                try {
                    String[] columns = parseCSVLine(line);
                    csvData.add(columns);
                        } catch (Exception e) {
                    log.error("Lỗi khi parse dòng {}: {}", rowNumber, e.getMessage());
                    errors.add(new ImportResultResponse.ImportError(
                        rowNumber, "parsing", "Lỗi parse: " + e.getMessage()));
                            errorCount++;
                        }
                    }
                    
        } catch (Exception e) {
            log.error("Lỗi khi đọc file CSV: {}", e.getMessage(), e);
            return new ImportResultResponse(false, "Lỗi khi đọc file CSV: " + e.getMessage(), 
                0, 0, 1, errors, results);
        }

        // Xử lý batch để tránh connection timeout
        int batchSize = 10; // Tăng batch size lên 10 user mỗi batch
        int totalRows = csvData.size();
        
        log.info("Bắt đầu xử lý {} dòng dữ liệu giảng viên với batch size {}", totalRows, batchSize);
        
        for (int i = 0; i < csvData.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, csvData.size());
            List<String[]> batch = csvData.subList(i, endIndex);
            
            log.info("Xử lý batch giảng viên {}-{} / {}", i + 1, endIndex, totalRows);
            
            try {
                BatchResult batchResult = processTeacherBatch(batch, i + 1);
                errors.addAll(batchResult.errors);
                results.addAll(batchResult.results);
                successCount += batchResult.successCount;
                errorCount += batchResult.errorCount;
                
                // Log progress
                log.info("Batch giảng viên {}-{} hoàn thành: {} thành công, {} lỗi", 
                    i + 1, endIndex, batchResult.successCount, batchResult.errorCount);
                
                // Thêm delay nhỏ giữa các batch để tránh quá tải
                if (i + batchSize < csvData.size()) {
                    Thread.sleep(100); // 100ms delay
                    }
                    
                } catch (Exception e) {
                log.error("Lỗi khi xử lý batch giảng viên {}-{}: {}", i + 1, endIndex, e.getMessage(), e);
                // Thêm lỗi cho tất cả dòng trong batch này
                    for (int j = i; j < endIndex; j++) {
                        errors.add(new ImportResultResponse.ImportError(
                        j + 1, "batch", "Lỗi batch: " + e.getMessage()));
                        errorCount++;
                    }
                }
            }
            
        String message = String.format("Import giảng viên hoàn thành: %d thành công, %d lỗi", successCount, errorCount);
        return new ImportResultResponse(true, message, totalRows, successCount, errorCount, errors, results);
    }

    /**
     * Xử lý một batch giảng viên nhỏ để tránh connection timeout
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BatchResult processTeacherBatch(List<String[]> batch, int startRow) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        List<ImportResultResponse.StudentImportResult> results = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        log.debug("Bắt đầu xử lý batch giảng viên {} dòng từ dòng {}", batch.size(), startRow);
        
        for (int i = 0; i < batch.size(); i++) {
            String[] columns = batch.get(i);
            int rowNumber = startRow + i;
            
            // Xử lý từng giảng viên riêng biệt để tránh rollback issues
            try {
                ImportResultResponse.StudentImportResult result = processSingleTeacher(columns, rowNumber);
                if (result != null) {
                    results.add(result);
                    successCount++;
                } else {
                    errorCount++;
                }
            } catch (Exception e) {
                log.error("Lỗi khi xử lý giảng viên ở dòng {}: {}", rowNumber, e.getMessage(), e);
                
                // Xử lý các loại lỗi khác nhau
                String errorType = "processing";
                String errorMessage = e.getMessage();
                
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("Connection reset") || 
                        e.getMessage().contains("Connection timed out") ||
                        e.getMessage().contains("AsyncRequestNotUsableException")) {
                        errorType = "connection";
                        errorMessage = "Lỗi kết nối - vui lòng thử lại";
                    } else if (e.getMessage().contains("timeout")) {
                        errorType = "timeout";
                        errorMessage = "Timeout - vui lòng thử lại";
                    } else if (e.getMessage().contains("constraint")) {
                        errorType = "constraint";
                        errorMessage = "Vi phạm ràng buộc dữ liệu";
                    } else if (e.getMessage().contains("rollback")) {
                        errorType = "transaction";
                        errorMessage = "Lỗi transaction - vui lòng thử lại";
                    }
                }
                
                errors.add(new ImportResultResponse.ImportError(
                    rowNumber, errorType, errorMessage));
                errorCount++;
            }
        }
        
        return new BatchResult(errors, results, successCount, errorCount);
    }

    /**
     * Xử lý một giảng viên đơn lẻ với transaction riêng biệt
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ImportResultResponse.StudentImportResult processSingleTeacher(String[] columns, int rowNumber) {
        try {
            if (columns.length < 3) {
                log.warn("Dòng {} không đủ cột dữ liệu (cần ít nhất 3 cột)", rowNumber);
                return null;
            }

            // Parse dữ liệu từ CSV (format: Họ tên, Username, Password)
            String fullName = columns[0].trim();
            String username = columns[1].trim();
            String password = columns[2].trim();

            // Validate dữ liệu
            List<String> validationErrors = validateTeacherData(fullName, username, password);

            if (!validationErrors.isEmpty()) {
                log.warn("Dòng {} có lỗi validation: {}", rowNumber, validationErrors);
                return null;
            }

            // Tạo user mới
            User user = createTeacherUser(fullName, username, password);
            
            // Tạo profile bất đồng bộ để tránh timeout với role TEACHER
            createProfileAsync(user.getUserId(), "TEACHER");

            return new ImportResultResponse.StudentImportResult(
                user.getUserId().toString(), fullName, username, true, "Tạo tài khoản giảng viên thành công", user.getUserId());
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý giảng viên ở dòng {}: {}", rowNumber, e.getMessage(), e);
            throw e; // Re-throw để được xử lý ở level cao hơn
        }
    }

    /**
     * Validate dữ liệu giảng viên
     */
    private List<String> validateTeacherData(String fullName, String username, String password) {
        List<String> errors = new ArrayList<>();
        
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.add("Họ tên không được để trống");
        }
        if (username == null || username.trim().isEmpty()) {
            errors.add("Username không được để trống");
        }
        if (password == null || password.trim().isEmpty()) {
            errors.add("Mật khẩu không được để trống");
        }

        // Kiểm tra username đã tồn tại
        if (username != null && !username.trim().isEmpty() && userRepository.existsByUsername(username)) {
            errors.add("Username đã tồn tại: " + username);
        }

        return errors;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public User createTeacherUser(String fullName, String username, String password) {
        try {
            // Create User entity
            User user = new User();
            user.setUsername(username);
            user.setFullName(fullName);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setStatus(1); // Active
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            // Set role TEACHER (ID = 3)
            Role teacherRole = entityManager.find(Role.class, 3);
            if (teacherRole != null) {
                user.getRoles().add(teacherRole);
            }
            
            // Save user using entityManager like createUser
            entityManager.persist(user);
            entityManager.flush();
            
            log.info("Đã tạo giảng viên: {} (ID: {})", username, user.getUserId());
            
            // Create profile asynchronously (không block) với role TEACHER
            try {
                createProfileAsync(user.getUserId(), "TEACHER");
            } catch (Exception e) {
                log.warn("Không thể tạo profile cho user {}: {}", user.getUserId(), e.getMessage());
                // Không throw exception, chỉ log warning
            }
            
            return user;
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo giảng viên {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Không thể tạo giảng viên: " + e.getMessage());
        }
    }

    /**
     * Class để chứa kết quả xử lý batch
     */
    private static class BatchResult {
        public final List<ImportResultResponse.ImportError> errors;
        public final List<ImportResultResponse.StudentImportResult> results;
        public final int successCount;
        public final int errorCount;

        public BatchResult(List<ImportResultResponse.ImportError> errors, 
                          List<ImportResultResponse.StudentImportResult> results, 
                          int successCount, int errorCount) {
            this.errors = errors;
            this.results = results;
            this.successCount = successCount;
            this.errorCount = errorCount;
        }
    }

}
