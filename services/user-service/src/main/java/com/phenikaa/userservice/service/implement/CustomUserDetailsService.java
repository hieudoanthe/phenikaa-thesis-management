package com.phenikaa.userservice.service.implement;

import com.phenikaa.userservice.config.CustomUserDetails;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .filter(Role::getIsActive)
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(user, authorities);
    }

}
