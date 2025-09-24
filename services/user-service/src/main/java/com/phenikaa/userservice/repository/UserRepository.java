package com.phenikaa.userservice.repository;

import com.phenikaa.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndPeriodId(String username, Integer periodId);
    List<User> findByPeriodId(Integer periodId);
    List<User> findAllByUsername(String username);
}
