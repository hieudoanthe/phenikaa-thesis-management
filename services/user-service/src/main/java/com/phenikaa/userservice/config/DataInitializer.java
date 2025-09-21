package com.phenikaa.userservice.config;

import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        try {
            // Kiểm tra xem đã có roles chưa
            if (roleRepository.count() == 0) {
                log.info("Khởi tạo dữ liệu roles...");
                
                // Tạo role STUDENT (ID: 1)
                Role studentRole = new Role();
                studentRole.setRoleName(Role.RoleName.STUDENT);
                studentRole.setIsActive(true);
                roleRepository.save(studentRole);
                
                // Tạo role ADMIN (ID: 2)
                Role adminRole = new Role();
                adminRole.setRoleName(Role.RoleName.ADMIN);
                adminRole.setIsActive(true);
                roleRepository.save(adminRole);
                
                // Tạo role TEACHER (ID: 3)
                Role teacherRole = new Role();
                teacherRole.setRoleName(Role.RoleName.TEACHER);
                teacherRole.setIsActive(true);
                roleRepository.save(teacherRole);
                
                log.info("Đã khởi tạo thành công 3 roles: STUDENT, ADMIN, TEACHER");
            } else {
                log.info("Roles đã tồn tại, bỏ qua khởi tạo");
            }
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo roles: {}", e.getMessage(), e);
        }
    }
}
