package com.phenikaa.userservice.service.implement;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoDTO;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.mapper.UserMapper;
import com.phenikaa.userservice.repository.UserRepository;
import com.phenikaa.userservice.service.interfaces.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
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
    public User saveUser(CreateUserRequest createUserRequest) {
        User user = userMapper.toEntity(createUserRequest, entityManager);

        if (createUserRequest.getPassword() != null && !createUserRequest.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword());
            user.setPasswordHash(encodedPassword);
        } else {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        entityManager.persist(user);

        return user;
    }


    public Optional<UserInfoDTO> verifyUser(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                .map(user -> new UserInfoDTO(
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

}
