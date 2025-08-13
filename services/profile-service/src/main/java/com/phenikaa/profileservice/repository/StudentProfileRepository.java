package com.phenikaa.profileservice.repository;

import com.phenikaa.profileservice.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository <StudentProfile, Integer>{
}
