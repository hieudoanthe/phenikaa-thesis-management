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
    Page<GetUserResponse> getAllUsers(int page, int size);
    GetUserResponse getUserById(Integer userId);
    List<GetUserResponse> getUserByIds(List<Integer> userIds);
    
    // Thêm method mới cho filter theo specification
    Page<GetUserResponse> filterUsers(UserFilterRequest filterRequest);
    
    // Các method filter đơn giản
    List<GetUserResponse> searchUsersByPattern(String searchPattern);
    List<GetUserResponse> getUsersByRole(String roleName);
    List<GetUserResponse> getUsersByStatus(Integer status);
    
    // Dynamic filter method
    Page<GetUserResponse> dynamicFilterUsers(DynamicFilterRequest dynamicFilterRequest);
    
    // Statistics methods
    Long getUserCount();
    Long getUserCountByRole(String role);
    Long getUserCountByStatus(Integer status);
    Long getActiveUsersToday();
}
