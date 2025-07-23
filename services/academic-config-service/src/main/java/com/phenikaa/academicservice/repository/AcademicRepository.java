package com.phenikaa.academicservice.repository;

import com.phenikaa.academicservice.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface AcademicRepository extends JpaRepository<AcademicYear,Integer> {
    Optional<AcademicYear> findByYearIdAndYearName(Integer yearId, String yearName);
    List<AcademicYear> findByYearId(Integer yearId);
    List<AcademicYear> findAll();
}
