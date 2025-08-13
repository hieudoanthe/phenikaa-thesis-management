package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.userservice.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    User createUser(CreateUserRequest createUserRequest);
    UserInfoResponse verifyUser(LoginRequest request);
    List<GetUserResponse> getAllUsers();
    void deleteUser(Integer userId);
    void updateUser(UpdateUserRequest updateUserRequest);
    void changeStatusUser(Integer userId);
    Page<GetUserResponse> getAllUsers(int page, int size);
}
