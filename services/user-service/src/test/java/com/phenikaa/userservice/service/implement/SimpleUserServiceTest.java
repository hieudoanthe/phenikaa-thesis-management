package com.phenikaa.userservice.service.implement;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.mapper.UserMapper;
import com.phenikaa.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test đơn giản cho UserService với 3 test cases chính:
 * 1. Test lấy danh sách user thành công
 * 2. Test lấy user theo ID thành công
 * 3. Test thay đổi trạng thái user thành công
 */
@ExtendWith(MockitoExtension.class)
class SimpleUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private GetUserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        Role testRole = new Role();
        testRole.setRoleId(1);
        testRole.setRoleName(Role.RoleName.STUDENT);

        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setPasswordHash("encodedPassword");
        testUser.setStatus(1);
        testUser.setRoles(Set.of(testRole));

        testUserResponse = new GetUserResponse();
        testUserResponse.setUserId(1);
        testUserResponse.setUsername("testuser");
        testUserResponse.setFullName("Test User");
        testUserResponse.setStatus(1);
    }

    /**
     * Test Case 1: Lấy danh sách tất cả user thành công
     * Kiểm tra method getAllUsers() trả về danh sách user
     */
    @Test
    void testGetAllUsers_Success() {
        // Given - Chuẩn bị dữ liệu test
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserResponse);

        // When - Thực hiện hành động test
        List<GetUserResponse> result = userService.getAllUsers();

        // Then - Kiểm tra kết quả
        assertNotNull(result, "Danh sách user không được null");
        assertEquals(1, result.size(), "Số lượng user phải là 1");
        assertEquals("testuser", result.get(0).getUsername(), "Username phải khớp");
        assertEquals("Test User", result.get(0).getFullName(), "Full name phải khớp");

        // Verify - Kiểm tra các method được gọi
        verify(userRepository).findAll();
        verify(userMapper).toDTO(testUser);
    }

    /**
     * Test Case 2: Lấy user theo ID thành công
     * Kiểm tra method getUserById() trả về user đúng
     */
    @Test
    void testGetUserById_Success() {
        // Given - Chuẩn bị dữ liệu test
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserResponse);

        // When - Thực hiện hành động test
        GetUserResponse result = userService.getUserById(1);

        // Then - Kiểm tra kết quả
        assertNotNull(result, "User response không được null");
        assertEquals("testuser", result.getUsername(), "Username phải khớp");
        assertEquals("Test User", result.getFullName(), "Full name phải khớp");
        assertEquals(1, result.getUserId(), "User ID phải khớp");

        // Verify - Kiểm tra các method được gọi
        verify(userRepository).findById(1);
        verify(userMapper).toDTO(testUser);
    }

    /**
     * Test Case 3: Thay đổi trạng thái user thành công
     * Kiểm tra method changeStatusUser() thay đổi status từ 1 sang 2
     */
    @Test
    void testChangeStatusUser_Success() {
        // Given - Chuẩn bị dữ liệu test
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When - Thực hiện hành động test
        userService.changeStatusUser(1);

        // Then - Kiểm tra kết quả
        assertEquals(2, testUser.getStatus(), "Status phải được thay đổi từ 1 sang 2");

        // Verify - Kiểm tra các method được gọi
        verify(userRepository).findById(1);
        verify(userRepository).save(testUser);
    }

    /**
     * Test Case bổ sung: Lấy user theo ID không tồn tại
     * Kiểm tra exception khi user không tồn tại
     */
    @Test
    void testGetUserById_UserNotFound_ThrowsException() {
        // Given - Chuẩn bị dữ liệu test
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then - Thực hiện và kiểm tra exception
        assertThrows(org.springframework.web.server.ResponseStatusException.class, 
            () -> userService.getUserById(999),
            "Phải throw exception khi user không tồn tại");

        // Verify - Kiểm tra các method được gọi
        verify(userRepository).findById(999);
        verify(userMapper, never()).toDTO(any(User.class));
    }

    /**
     * Test Case bổ sung: Lấy danh sách user có phân trang
     * Kiểm tra method getAllUsers(page, size) trả về Page
     */
    @Test
    void testGetAllUsersPaged_Success() {
        // Given - Chuẩn bị dữ liệu test
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), PageRequest.of(0, 8), 1);
        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserResponse);

        // When - Thực hiện hành động test
        Page<GetUserResponse> result = userService.getAllUsers(0, 8);

        // Then - Kiểm tra kết quả
        assertNotNull(result, "Page result không được null");
        assertEquals(1, result.getTotalElements(), "Tổng số elements phải là 1");
        assertEquals(1, result.getContent().size(), "Số lượng content phải là 1");
        assertEquals("testuser", result.getContent().get(0).getUsername(), "Username phải khớp");

        // Verify - Kiểm tra các method được gọi
        verify(userRepository).findAll(any(org.springframework.data.domain.Pageable.class));
        verify(userMapper).toDTO(testUser);
    }
}
