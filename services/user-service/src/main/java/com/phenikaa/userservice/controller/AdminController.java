package com.phenikaa.userservice.controller;

import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.userservice.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/saveUser")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
        User savedUser = userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUsers")
    public List<GetUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/getUsersPaged")
    public Page<GetUserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return userService.getAllUsers(page, size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteUser")
    public void deleteUser(@RequestParam Integer userId) {
        userService.deleteUser(userId);
    }

    @PutMapping("updateUser")
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest request) {
        userService.updateUser(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/changeStatusUser")
    public ResponseEntity<Void> changeStatusUser(@RequestParam Integer userId) {
        userService.changeStatusUser(userId);
        return ResponseEntity.noContent().build();
    }

}