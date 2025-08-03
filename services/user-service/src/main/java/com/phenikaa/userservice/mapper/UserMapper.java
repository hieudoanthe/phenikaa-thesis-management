package com.phenikaa.userservice.mapper;

import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.entity.User;
import jakarta.persistence.EntityManager;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "roles", expression = "java(mapRoleIdsToRoles(createUserRequest.getRoleIds(), entityManager))")
    User toEntity(CreateUserRequest createUserRequest, @Context EntityManager entityManager);

    GetUserResponse toDTO(User user);

    // Phương thức để lấy các Role từ roleIds
    default Set<Role> mapRoleIdsToRoles(Set<Integer> roleIds, @Context EntityManager em) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }

        // Tìm các Role từ cơ sở dữ liệu thông qua EntityManager
        return roleIds.stream()
                .map(roleId -> em.find(Role.class, roleId))
                .collect(Collectors.toSet());
    }
}

