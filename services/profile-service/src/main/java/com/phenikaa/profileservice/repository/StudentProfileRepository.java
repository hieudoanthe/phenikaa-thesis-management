package com.phenikaa.profileservice.repository;

import com.phenikaa.profileservice.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository <StudentProfile, String>{
    Optional<StudentProfile> findByUserId(Integer userId);
    Boolean existsByUserId(Integer userId);
    void deleteByUserId(Integer userId);
    
    // Statistics methods
    List<StudentProfile> findByMajor(String major);
    List<StudentProfile> findByAcademicYear(Integer academicYear);
}
