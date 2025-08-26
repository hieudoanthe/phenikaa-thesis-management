package com.phenikaa.userservice.service.implement;

import com.phenikaa.dto.request.CreateProfileRequest;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.userservice.client.ProfileServiceClient;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.userservice.dto.request.UserFilterRequest;
import com.phenikaa.userservice.dto.request.DynamicFilterRequest;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.mapper.UserMapper;
import com.phenikaa.userservice.repository.UserRepository;
import com.phenikaa.userservice.service.interfaces.UserService;
import com.phenikaa.userservice.specification.UserSpecification;
import com.phenikaa.userservice.filter.DynamicFilterBuilder;
import com.phenikaa.userservice.filter.DynamicQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;
    private final ProfileServiceClient profileServiceClient;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(CreateUserRequest createUserRequest) {
        User user = userMapper.toEntity(createUserRequest, entityManager);

        if (createUserRequest.getPassword() != null && !createUserRequest.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword());
            user.setPasswordHash(encodedPassword);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be empty!");
        }

        entityManager.persist(user);

        for (Role role : user.getRoles()) {
            String roleName = String.valueOf(role.getRoleName());
            profileServiceClient.createProfile(new CreateProfileRequest(user.getUserId(), roleName));
        }

        return user;
    }

    @Override
    public AuthenticatedUserResponse verifyUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password!");
        }

        if (user.getStatus() == 2) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked!");
        }

        return new AuthenticatedUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRoles().stream()
                        .map(role -> role.getRoleName().name())
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<GetUserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    public void deleteUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        userRepository.delete(userOpt.get());
        profileServiceClient.deleteProfile(userId);
    }

    @Override
    public void updateUser(UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(updateUserRequest.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        userMapper.toEntity(updateUserRequest, user);

        if (updateUserRequest.getRoleIds() != null) {
            Set<Role> roles = updateUserRequest.getRoleIds().stream()
                    .map(id -> {
                        Role role = new Role();
                        role.setRoleId(id);
                        return role;
                    })
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        userRepository.save(user);

        // Đồng bộ profile-service theo role mới
        Set<Role> updatedRoles = user.getRoles();
        boolean hasTeacher = updatedRoles.stream().anyMatch(r -> r.getRoleName() == Role.RoleName.TEACHER);
        boolean hasStudent = updatedRoles.stream().anyMatch(r -> r.getRoleName() == Role.RoleName.STUDENT);

        // Xóa profile cũ (gọi 2 lần để đảm bảo xóa cả student/teacher nếu tồn tại)
        try {
            profileServiceClient.deleteProfile(user.getUserId());
        } catch (Exception ignored) {}
        try {
            profileServiceClient.deleteProfile(user.getUserId());
        } catch (Exception ignored) {}

        // Tạo lại profile theo role mới
        if (hasStudent) {
            profileServiceClient.createProfile(new CreateProfileRequest(user.getUserId(), "STUDENT"));
        }
        if (hasTeacher) {
            profileServiceClient.createProfile(new CreateProfileRequest(user.getUserId(), "TEACHER"));
        }
    }

    @Override
    public void changeStatusUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        User user = userOpt.get();
        user.setStatus(user.getStatus() == 1 ? 2 : 1);
        userRepository.save(user);
    }

    @Override
    public Page<GetUserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId").ascending());
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    @Override
    public GetUserResponse getUserById(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        return userMapper.toDTO(userOpt.get());
    }

    @Override
    public List<GetUserResponse> getUserByIds(List<Integer> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    public Page<GetUserResponse> filterUsers(UserFilterRequest filterRequest) {
        // Tạo specification từ filter request
        Specification<User> spec = UserSpecification.withFilter(filterRequest);
        
        // Tạo Pageable với sorting
        Sort sort = Sort.by(
            filterRequest.getSortDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC,
            filterRequest.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
            filterRequest.getPage(), 
            filterRequest.getSize(), 
            sort
        );
        
        // Thực hiện query với specification và pageable
        Page<User> userPage = userRepository.findAll(spec, pageable);

        return userPage.map(userMapper::toDTO);
    }

    @Override
    public List<GetUserResponse> searchUsersByPattern(String searchPattern) {
        Specification<User> spec = UserSpecification.withSearchPattern(searchPattern);
        List<User> users = userRepository.findAll(spec);
        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    public List<GetUserResponse> getUsersByRole(String roleName) {
        try {
            Role.RoleName role = Role.RoleName.valueOf(roleName.toUpperCase());
            Specification<User> spec = UserSpecification.withRole(role);
            List<User> users = userRepository.findAll(spec);
            return users.stream()
                    .map(userMapper::toDTO)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vai trò không hợp lệ: " + roleName);
        }
    }

    @Override
    public List<GetUserResponse> getUsersByStatus(Integer status) {
        Specification<User> spec = UserSpecification.withStatus(status);
        List<User> users = userRepository.findAll(spec);
        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    public Page<GetUserResponse> dynamicFilterUsers(DynamicFilterRequest dynamicFilterRequest) {
        // Validate request
        List<String> validationErrors = DynamicQueryBuilder.validateRequest(dynamicFilterRequest);
        if (!validationErrors.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Validation errors: " + String.join(", ", validationErrors)
            );
        }

        Specification<User> spec = DynamicFilterBuilder.buildSpecification(dynamicFilterRequest);

        Pageable pageable = DynamicQueryBuilder.buildPageable(dynamicFilterRequest);

        Page<User> userPage = userRepository.findAll(spec, pageable);

        return userPage.map(userMapper::toDTO);
    }

}
