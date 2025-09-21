package com.phenikaa.userservice.mapper;

import com.phenikaa.dto.response.AuthenticatedUserResponse;
import com.phenikaa.userservice.dto.request.CreateUserRequest;
import com.phenikaa.userservice.dto.request.UpdateUserRequest;
import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.userservice.entity.Role;
import com.phenikaa.userservice.entity.User;
import jakarta.persistence.EntityManager;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "roles", expression = "java(mapRoleIdsToRoles(createUserRequest.getRoleIds(), entityManager))")
    @Mapping(target = "periodId", source = "periodId")
    User toEntity(CreateUserRequest createUserRequest, @Context EntityManager entityManager);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toEntity(UpdateUserRequest updateUserRequest, @MappingTarget User user);

    @Mapping(target = "roleIds", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "periodDescription", ignore = true)
    @Mapping(target = "periodIds", ignore = true)
    @Mapping(target = "totalRegistrations", ignore = true)
    GetUserResponse toDTO(User user);
    
    // Method để map với thông tin đợt đăng ký
    default GetUserResponse toDTOWithPeriodInfo(User user, String periodDescription, List<Integer> periodIds, Integer totalRegistrations) {
        GetUserResponse response = toDTO(user);
        response.setPeriodDescription(periodDescription);
        response.setPeriodIds(periodIds);
        response.setTotalRegistrations(totalRegistrations);
        return response;
    }

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

    default List<Integer> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(Role::getRoleId)
                .collect(Collectors.toList());
    }

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "mapRoleNames")
    AuthenticatedUserResponse toUserInfoResponse(User user);

    @Named("mapRoleNames")
    default List<String> mapRoleNames(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toList());
    }

}

