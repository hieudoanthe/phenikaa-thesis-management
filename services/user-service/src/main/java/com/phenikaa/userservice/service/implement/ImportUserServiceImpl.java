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
        int batchSize = 5; // Xử lý 5 user mỗi batch
        int totalRows = csvData.size();
        
        for (int i = 0; i < csvData.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, csvData.size());
            List<String[]> batch = csvData.subList(i, endIndex);
            
            try {
                BatchResult batchResult = processBatch(batch, i + 1, periodId);
                errors.addAll(batchResult.errors);
                results.addAll(batchResult.results);
                successCount += batchResult.successCount;
                errorCount += batchResult.errorCount;
            } catch (Exception e) {
                log.error("Lỗi khi xử lý batch {}-{}: {}", i + 1, endIndex, e.getMessage());
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult processBatch(List<String[]> batch, int startRow, Integer periodId) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        List<ImportResultResponse.StudentImportResult> results = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < batch.size(); i++) {
            String[] columns = batch.get(i);
            int rowNumber = startRow + i;
            
            try {
                if (columns.length < 3) {
                    errors.add(new ImportResultResponse.ImportError(
                        rowNumber, "format", "Dòng không đủ cột dữ liệu (cần ít nhất 3 cột)"));
                    errorCount++;
                    continue;
                }

                // Parse dữ liệu từ CSV (format: Họ tên, Username, Password)
                String fullName = columns[0].trim();
                String username = columns[1].trim(); // Username chính là email
                String password = columns[2].trim();

                // Validate dữ liệu
                List<String> validationErrors = validateStudentData(fullName, username, password, periodId);

                if (!validationErrors.isEmpty()) {
                    for (String error : validationErrors) {
                        errors.add(new ImportResultResponse.ImportError(
                            rowNumber, "validation", error));
                    }
                    errorCount++;
                    continue;
                }

                // Tạo user mới
                User user = createUser(fullName, username, password, periodId);
                
                // Tạo profile bất đồng bộ để tránh timeout
                createProfileAsync(user.getUserId());

                results.add(new ImportResultResponse.StudentImportResult(
                    user.getUserId().toString(), fullName, username, true, "Tạo tài khoản thành công", user.getUserId()));
                successCount++;

            } catch (Exception e) {
                log.error("Lỗi khi xử lý dòng {}: {}", rowNumber, e.getMessage());
                errors.add(new ImportResultResponse.ImportError(
                    rowNumber, "processing", "Lỗi xử lý: " + e.getMessage()));
                errorCount++;
            }
        }
        
        return new BatchResult(errors, results, successCount, errorCount);
    }

    /**
     * Tạo user mới với transaction riêng
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User createUser(String fullName, String username, String password, Integer periodId) {
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
        
        return user;
    }

    /**
     * Tạo profile bất đồng bộ để tránh timeout
     */
    @Async
    public CompletableFuture<Void> createProfileAsync(Integer userId) {
        try {
            profileServiceClient.createProfile(new CreateProfileRequest(userId, "STUDENT"));
            log.info("Tạo profile thành công cho user: {}", userId);
        } catch (Exception profileError) {
            log.warn("Không thể tạo profile cho user {}: {}", userId, profileError.getMessage());
            // Không throw exception, chỉ log warning
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public ImportResultResponse importTeachersFromCSV(MultipartFile file) {
        ImportResultResponse response = new ImportResultResponse();
        response.setSuccess(true);
        response.setMessage("Import giảng viên thành công");
        
        try {
            log.info("Bắt đầu import giảng viên từ CSV: {}", file.getOriginalFilename());
            
            // Parse CSV
            List<String[]> csvData = parseCSV(file);
            if (csvData.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("File CSV trống");
                return response;
            }
            
            // Validate header
            String[] headers = csvData.get(0);
            if (headers.length != 3 || 
                !headers[0].trim().equals("Họ và tên") ||
                !headers[1].trim().equals("Username") ||
                !headers[2].trim().equals("Mật khẩu")) {
                response.setSuccess(false);
                response.setMessage("File CSV phải có đúng 3 cột: Họ và tên, Username, Mật khẩu");
                return response;
            }
            
            // Process in batches to avoid timeout (like students)
            List<String[]> dataRows = csvData.subList(1, csvData.size()); // Skip header
            int successCount = 0;
            int errorCount = 0;
            List<ImportResultResponse.ImportError> errors = new ArrayList<>();
            
            int batchSize = 5; // Process 5 teachers per batch
            for (int i = 0; i < dataRows.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, dataRows.size());
                List<String[]> batch = dataRows.subList(i, endIndex);
                
                try {
                    // Process each teacher in batch
                    for (int j = 0; j < batch.size(); j++) {
                        String[] row = batch.get(j);
                        int rowNumber = i + j + 2; // +2 because we skip header and start from 1
                        
                        try {
                            if (row.length < 3) {
                                errors.add(new ImportResultResponse.ImportError(rowNumber, "validation", "Thiếu thông tin"));
                                errorCount++;
                                continue;
                            }
                            
                            String fullName = row[0].trim();
                            String username = row[1].trim();
                            String password = row[2].trim();
                            
                            // Validate data
                            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                                errors.add(new ImportResultResponse.ImportError(rowNumber, "validation", "Thông tin không được để trống"));
                                errorCount++;
                                continue;
                            }
                            
                            // Check if username already exists
                            if (userRepository.existsByUsername(username)) {
                                errors.add(new ImportResultResponse.ImportError(rowNumber, "validation", "Username đã tồn tại"));
                                errorCount++;
                                continue;
                            }
                            
                            // Create teacher user with new transaction
                            createTeacherUser(fullName, username, password);
                            successCount++;
                            
                        } catch (Exception e) {
                            log.error("Lỗi khi xử lý dòng {}: {}", rowNumber, e.getMessage());
                            errors.add(new ImportResultResponse.ImportError(rowNumber, "processing", e.getMessage()));
                            errorCount++;
                        }
                    }
                    
                    // Small delay between batches to avoid overwhelming the system
                    if (endIndex < dataRows.size()) {
                        Thread.sleep(100); // 100ms delay
                    }
                    
                } catch (Exception e) {
                    log.error("Lỗi khi xử lý batch {}-{}: {}", i + 1, endIndex, e.getMessage());
                    // Add error for all rows in this batch
                    for (int j = i; j < endIndex; j++) {
                        errors.add(new ImportResultResponse.ImportError(
                            j + 2, "batch", "Lỗi batch: " + e.getMessage()));
                        errorCount++;
                    }
                }
            }
            
            response.setSuccessCount(successCount);
            response.setErrorCount(errorCount);
            response.setErrors(errors);
            response.setMessage(String.format("Import hoàn thành: %d thành công, %d lỗi", successCount, errorCount));
            
            log.info("Import giảng viên hoàn thành: {} thành công, {} lỗi", successCount, errorCount);
            
        } catch (Exception e) {
            log.error("Lỗi khi import giảng viên từ CSV: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Lỗi khi import giảng viên: " + e.getMessage());
        }
        
        return response;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
            
            // Set TEACHER role
            Role teacherRole = roleRepository.findByRoleName(Role.RoleName.TEACHER)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role TEACHER"));
            user.setRoles(Set.of(teacherRole));
            
            // Save user using entityManager like createUser
            entityManager.persist(user);
            entityManager.flush();
            
            log.info("Đã tạo giảng viên: {} (ID: {})", username, user.getUserId());
            
            // Create profile asynchronously
            createProfileAsync(user.getUserId());
            
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
