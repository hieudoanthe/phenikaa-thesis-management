package com.phenikaa.profileservice.repository;

import com.phenikaa.profileservice.entity.StudentProfile;
import com.phenikaa.profileservice.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, String> {
    Optional<TeacherProfile> findByUserId(Integer userId);
    Boolean existsByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
