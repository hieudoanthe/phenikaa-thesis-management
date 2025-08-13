package com.phenikaa.userservice.service.implement;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.userservice.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.mapper.UserMapper;
import com.phenikaa.userservice.repository.UserRepository;
import com.phenikaa.userservice.service.interfaces.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        return user;
    }

    @Override
    public UserInfoResponse verifyUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password!");
        }

        if (user.getStatus() == 2) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked!");
        }

        return new UserInfoResponse(
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
    public Page<GetUserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId").ascending());
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

}
