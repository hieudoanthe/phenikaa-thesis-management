package com.phenikaa.userservice.service.implement;

import com.phenikaa.common.dto.UserDto;
import com.phenikaa.userservice.dao.interfaces.UserDao;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public User save(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userDao.save(user);
    }

    @Override
    public UserDto getUserById(Integer id) {
        UserDto user = new UserDto();
        user.setId(id);
        return user;
    }
}

