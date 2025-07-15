package com.phenikaa.userservice.service.implement;

import com.phenikaa.userservice.dao.interfaces.RoleDao;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleDao roleDao;

    @Override
    public Optional<Role> findByRoleName(String roleName) {
        return roleDao.findByRoleName(roleName);
    }
}
