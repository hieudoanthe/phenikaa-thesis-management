package com.phenikaa.profileservice.repository;

import com.phenikaa.profileservice.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<TeacherProfile, Integer> {
}
