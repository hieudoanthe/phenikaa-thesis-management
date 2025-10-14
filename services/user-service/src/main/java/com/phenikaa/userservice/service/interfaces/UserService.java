package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.userservice.dto.request.UserFilterRequest;
import com.phenikaa.userservice.dto.request.DynamicFilterRequest;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    User createUser(CreateUserRequest createUserRequest);
    AuthenticatedUserResponse verifyUser(LoginRequest request);
    void deleteUser(Integer userId);
    void updateUser(UpdateUserRequest updateUserRequest);
    void changeStatusUser(Integer userId);
    List<GetUserResponse> getAllUsers();
    GetUserResponse getUserById(Integer userId);
    List<GetUserResponse> getUserByIds(List<Integer> userIds);
    Page<GetUserResponse> filterUsers(UserFilterRequest filterRequest);
    List<GetUserResponse> searchUsersByPattern(String searchPattern);
    List<GetUserResponse> getUsersByRole(String roleName);
    List<GetUserResponse> getUsersByStatus(Integer status);
    Page<GetUserResponse> dynamicFilterUsers(DynamicFilterRequest dynamicFilterRequest);
    Long getUserCount();
    Long getUserCountByRole(String role);
    Long getStudentCountByPeriod(Integer periodId);
    Page<GetUserResponse> getAllUsersGroupedByUsername(org.springframework.data.domain.Pageable pageable);
    
    // Password reset methods
    String createPasswordResetToken(String username);
    boolean validatePasswordResetToken(String token);
    boolean resetPasswordWithToken(String token, String newPassword);
    Integer getUserIdFromToken(String token);

    // Change password
    void changePassword(Integer userId, String currentPassword, String newPassword);
}
