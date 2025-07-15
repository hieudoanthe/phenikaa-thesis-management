package com.phenikaa.userservice.dao.implement;

import com.phenikaa.userservice.dao.interfaces.RoleDao;
import com.phenikaa.userservice.entity.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleDaoImpl implements RoleDao {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Optional<Role> findByRoleName(String roleName) {
        TypedQuery<Role> query = entityManager.createQuery("SELECT r FROM Role r WHERE r.roleName = :roleName", Role.class);
        query.setParameter("roleName", roleName);
        return Optional.ofNullable(query.getSingleResult());
    }
}
