package com.phenikaa.userservice.service.implement;

import com.phenikaa.userservice.dto.request.UserRequest;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.mapper.UserMapper;
import com.phenikaa.userservice.service.interfaces.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public User saveUser(UserRequest userRequest) {
        // Chuyển đổi DTO thành Entity
        User user = userMapper.toEntity(userRequest, entityManager);

        // Mã hóa mật khẩu trước khi lưu
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
            user.setPasswordHash(encodedPassword);
        } else {
            throw new IllegalArgumentException("Password không được để trống");
        }

        // Lưu entity vào DB
        entityManager.persist(user);

        return user;
    }
}
