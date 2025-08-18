package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
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
}
