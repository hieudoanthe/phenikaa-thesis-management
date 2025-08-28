package com.phenikaa.userservice.controller;

import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.userservice.dto.request.UserFilterRequest;
import com.phenikaa.userservice.dto.request.DynamicFilterRequest;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.dto.response.UserFilterResponse;
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

    @PostMapping("/save-user")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
        User savedUser = userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/get-users")
    public List<GetUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/get-users-paged")
    public Page<GetUserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return userService.getAllUsers(page, size);
    }

    @DeleteMapping("/delete-user")
    public void deleteUser(@RequestParam Integer userId) {
        userService.deleteUser(userId);
    }

    @PutMapping("update-user")
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest request) {
        userService.updateUser(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/change-status-user")
    public ResponseEntity<Void> changeStatusUser(@RequestParam Integer userId) {
        userService.changeStatusUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/filter-users")
    public ResponseEntity<UserFilterResponse> filterUsers(@RequestBody UserFilterRequest filterRequest) {
        Page<GetUserResponse> filteredUsers = userService.filterUsers(filterRequest);
        UserFilterResponse response = UserFilterResponse.fromPage(filteredUsers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-users")
    public ResponseEntity<List<GetUserResponse>> searchUsers(@RequestParam String searchPattern) {
        List<GetUserResponse> users = userService.searchUsersByPattern(searchPattern);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users-by-role")
    public ResponseEntity<List<GetUserResponse>> getUsersByRole(@RequestParam String roleName) {
        List<GetUserResponse> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users-by-status")
    public ResponseEntity<List<GetUserResponse>> getUsersByStatus(@RequestParam Integer status) {
        List<GetUserResponse> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/dynamic-filter-users")
    public ResponseEntity<UserFilterResponse> dynamicFilterUsers(@RequestBody DynamicFilterRequest dynamicFilterRequest) {
        Page<GetUserResponse> filteredUsers = userService.dynamicFilterUsers(dynamicFilterRequest);
        UserFilterResponse response = UserFilterResponse.fromPage(filteredUsers);
        return ResponseEntity.ok(response);
    }

}