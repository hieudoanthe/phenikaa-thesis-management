package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.userservice.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(CreateUserRequest createUserRequest);
    Optional<UserInfoResponse> verifyUser(LoginRequest request);
    List<GetUserResponse> getAllUsers();
    void deleteUser(Integer userId);
    User updateUser(UpdateUserRequest updateUserRequest);
}
