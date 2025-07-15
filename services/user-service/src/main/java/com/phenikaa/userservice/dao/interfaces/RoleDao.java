package com.phenikaa.userservice.dao.interfaces;

import com.phenikaa.userservice.entity.Role;

import java.util.Optional;

public interface RoleDao {
    Optional<Role> findByRoleName(String roleName);
}
