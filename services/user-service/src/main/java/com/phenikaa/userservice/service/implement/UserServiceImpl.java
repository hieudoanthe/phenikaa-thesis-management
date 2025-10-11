package com.phenikaa.userservice.service.implement;

import com.phenikaa.dto.request.CreateProfileRequest;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.userservice.client.ProfileServiceClient;
import com.phenikaa.userservice.config.CustomUserDetails;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.userservice.dto.request.UserFilterRequest;
import com.phenikaa.userservice.dto.request.DynamicFilterRequest;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.mapper.UserMapper;
import com.phenikaa.userservice.repository.UserRepository;
import com.phenikaa.userservice.repository.RefreshTokenRepository;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;
    private final ProfileServiceClient profileServiceClient;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

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
        createProfileAsync(user);

        return user;
    }

    @Override
    public AuthenticatedUserResponse verifyUser(LoginRequest request) {
        // 1. Gửi username + password cho AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Lấy thông tin user từ Authentication (được DaoAuthenticationProvider xử lý)
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. Kiểm tra status user (nếu cần)
        if (userDetails.getUserId() != null) {
            User user = userRepository.findById(userDetails.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
            if (user.getStatus() == 2) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked!");
            }
        }

        return new AuthenticatedUserResponse(
                userDetails.getUserId(),
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
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
    @Transactional
    public void deleteUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        try {
            refreshTokenRepository.deleteByUser_UserId(userId);
        } catch (Exception ignored) {}

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
    public GetUserResponse getUserById(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        User user = userOpt.get();
        if (user.getUsername() != null) {
            try {
                List<User> sameUsernameUsers = userRepository.findAllByUsername(user.getUsername());
                List<Integer> periodIds = sameUsernameUsers.stream()
                        .map(User::getPeriodId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                String periodDescription;
                if (periodIds.isEmpty()) {
                    boolean isStudent = user.getRoles() != null && user.getRoles().stream()
                            .anyMatch(role -> role.getRoleName().name().equals("STUDENT"));
                    periodDescription = isStudent ? "Chưa đăng ký đợt nào" : "";
                } else {
                    periodDescription = "Đợt " + periodIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
                }
                return userMapper.toDTOWithPeriodInfo(user, periodDescription, periodIds, sameUsernameUsers.size());
            } catch (Exception ignored) {}
        }
        return userMapper.toDTO(user);
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
        Specification<User> spec = UserSpecification.withFilter(filterRequest);

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
        List<String> validationErrors = DynamicQueryBuilder.getInstance().validateRequest(dynamicFilterRequest);
        if (!validationErrors.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Validation errors: " + String.join(", ", validationErrors)
            );
        }

        Specification<User> spec = DynamicFilterBuilder.buildSpecification(dynamicFilterRequest);

        Pageable pageable = DynamicQueryBuilder.getInstance().buildPageable(dynamicFilterRequest);

        Page<User> userPage = userRepository.findAll(spec, pageable);

        return userPage.map(userMapper::toDTO);
    }

    @Override
    public Long getUserCount() {
        return userRepository.count();
    }

    @Override
    public Long getUserCountByRole(String role) {
        try {
            Role.RoleName roleName = Role.RoleName.valueOf(role.toUpperCase());
            Specification<User> spec = UserSpecification.withRole(roleName);
            return userRepository.count(spec);
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }

    @Override
    public Long getStudentCountByPeriod(Integer periodId) {
        try {
            Specification<User> spec = UserSpecification.withRole(Role.RoleName.STUDENT)
                .and(UserSpecification.withPeriodId(periodId));
            return userRepository.count(spec);
        } catch (Exception e) {
            return 0L;
        }
    }

    @Async
    public CompletableFuture<Void> createProfileAsync(User user) {
        try {
            for (Role role : user.getRoles()) {
                String roleName = String.valueOf(role.getRoleName());
                try {
                    profileServiceClient.createProfile(new CreateProfileRequest(user.getUserId(), roleName));
                    log.info("Tạo profile {} thành công cho người dùng: {}", roleName, user.getUserId());
                } catch (Exception e) {
                    log.warn("Không thể tạo profile {} cho user {}: {}", roleName, user.getUserId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi tạo profile cho user {}: {}", user.getUserId(), e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Page<GetUserResponse> getAllUsersGroupedByUsername(org.springframework.data.domain.Pageable pageable) {
        try {
            List<User> allUsers = userRepository.findAll();

            Map<String, List<User>> groupedByUsername = allUsers.stream()
                .collect(Collectors.groupingBy(User::getUsername));
            
            List<GetUserResponse> result = new ArrayList<>();
            
            for (Map.Entry<String, List<User>> entry : groupedByUsername.entrySet()) {
                List<User> users = entry.getValue();

                User firstUser = users.get(0);

                List<Integer> periodIds = users.stream()
                    .map(User::getPeriodId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

                String periodDescription;
                if (periodIds.isEmpty()) {
                    boolean isStudent = firstUser.getRoles().stream()
                        .anyMatch(role -> role.getRoleName().name().equals("STUDENT"));
                    periodDescription = isStudent ? "Chưa đăng ký đợt nào" : "";
                } else {
                    periodDescription = "Đợt " + periodIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
                }

                GetUserResponse userResponse = userMapper.toDTOWithPeriodInfo(
                    firstUser, 
                    periodDescription, 
                    periodIds, 
                    users.size()
                );
                
                result.add(userResponse);
            }

            result.sort((a, b) -> a.getUsername().compareTo(b.getUsername()));

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), result.size());
            List<GetUserResponse> pageContent = result.subList(start, end);
            
            return new org.springframework.data.domain.PageImpl<>(
                pageContent, 
                pageable, 
                result.size()
            );
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách người dùng nhóm theo tên đăng nhập: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách người dùng: " + e.getMessage());
        }
    }

}
