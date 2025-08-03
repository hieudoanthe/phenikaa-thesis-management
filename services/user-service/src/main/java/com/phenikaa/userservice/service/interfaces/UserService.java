package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoDTO;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(CreateUserRequest createUserRequest);
    Optional<UserInfoDTO> verifyUser(LoginRequest request);
    List<GetUserResponse> getAllUsers();
}
