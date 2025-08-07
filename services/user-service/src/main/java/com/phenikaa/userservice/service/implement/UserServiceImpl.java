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
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            throw new IllegalArgumentException("Password cannot be empty!");
        }

        entityManager.persist(user);

        return user;
    }


    public Optional<UserInfoResponse> verifyUser(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                .map(user -> new UserInfoResponse(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRoles().stream()
                                .map(role -> role.getRoleName().name())
                                .collect(Collectors.toList())
                ));
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
            throw new NotFoundException("User not found!");
        }
        userRepository.delete(userOpt.get());
    }

    @Override
    public User updateUser(UpdateUserRequest updateUserRequest) {
        Optional<User> userOpt = userRepository.findById(updateUserRequest.getUserId());

        if (userOpt.isPresent()){
            User user = userOpt.orElseThrow(() -> new NotFoundException("User not found!"));

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
            return userRepository.save(user);
        }
        else {
            throw new NotFoundException("User not found!");
        }
    }

}
