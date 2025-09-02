package com.phenikaa.evalservice.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "api-gateway",
        contextId = "thesisServiceClient"
        ,path = "/thesis-service",configuration = FeignTokenInterceptor.class)
public interface ThesisServiceClient {

    /**
     * Lấy danh sách sinh viên đã đăng ký đề tài theo đợt đăng ký
     */
    @GetMapping("/api/thesis-service/student-period/registered/{periodId}")
    List<Map<String, Object>> getRegisteredStudentsByPeriod(@PathVariable String periodId);

    /**
     * Lấy danh sách sinh viên đã đề xuất đề tài theo đợt đăng ký
     */
    @GetMapping("/api/thesis-service/student-period/suggested/{periodId}")
    List<Map<String, Object>> getSuggestedStudentsByPeriod(@PathVariable String periodId);

    /**
     * Lấy danh sách tất cả sinh viên (đăng ký + đề xuất) theo đợt đăng ký
     */
    @GetMapping("/api/thesis-service/student-period/all/{periodId}")
    List<Map<String, Object>> getAllStudentsByPeriod(@PathVariable String periodId);

    /**
     * Lấy danh sách sinh viên theo đợt đăng ký (alias cho /all/{periodId})
     */
    @GetMapping("/api/thesis-service/student-period/{periodId}")
    List<Map<String, Object>> getStudentsByPeriod(@PathVariable String periodId);

    /**
     * Lấy thông tin đề tài theo ID
     */
    @GetMapping("/api/thesis-service/topics/{topicId}")
    Map<String, Object> getTopicById(@PathVariable("topicId") Integer topicId);

    /**
     * Lấy danh sách đề tài đã được duyệt
     */
    @GetMapping("/api/thesis-service/admin/topics/approved")
    List<Map<String, Object>> getApprovedTopics();

    /**
     * Lấy danh sách đề tài theo năm học
     */
    @GetMapping("/api/thesis-service/topics/academic-year")
    List<Map<String, Object>> getTopicsByAcademicYear(@RequestParam("academicYearId") Integer academicYearId);

    /**
     * Lấy danh sách sinh viên đã đăng ký đề tài
     */
    @GetMapping("/api/thesis-service/registers/approved")
    List<Map<String, Object>> getApprovedRegistrations();

    /**
     * Lấy danh sách sinh viên đã đăng ký theo đợt
     */
    @GetMapping("/api/thesis-service/registers/period")
    List<Map<String, Object>> getRegistrationsByPeriod(@RequestParam("periodId") Integer periodId);

    /**
     * Lấy thông tin đợt đăng ký hiện tại
     */
    @GetMapping("/api/thesis-service/admin/current")
    Map<String, Object> getCurrentPeriod();
}
