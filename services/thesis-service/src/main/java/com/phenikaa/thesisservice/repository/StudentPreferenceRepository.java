package com.phenikaa.thesisservice.repository;

import com.phenikaa.thesisservice.entity.StudentPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentPreferenceRepository extends JpaRepository<StudentPreference, Integer> {
    Optional<StudentPreference> findByStudentId(Integer studentId);
}


