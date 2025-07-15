package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.userservice.entity.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByRoleName(String roleName);
}
