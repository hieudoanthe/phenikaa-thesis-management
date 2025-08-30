package com.phenikaa.academicservice.repository;

import com.phenikaa.academicservice.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface AcademicRepository extends JpaRepository<AcademicYear,Integer> {
    Optional<AcademicYear> findByYearIdAndYearName(Integer yearId, String yearName);
    List<AcademicYear> findByYearId(Integer yearId);
    List<AcademicYear> findAll();
    
    // Tìm năm học đang active (status = 1)
    Optional<AcademicYear> findByStatus(Integer status);
    
    // Tìm năm học theo trạng thái
    List<AcademicYear> findAllByStatus(Integer status);
}
