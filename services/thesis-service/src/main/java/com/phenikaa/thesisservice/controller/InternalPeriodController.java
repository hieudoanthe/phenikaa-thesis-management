package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.entity.RegistrationPeriod;
import com.phenikaa.thesisservice.service.interfaces.RegistrationPeriodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal/periods")
public class InternalPeriodController {

    private final RegistrationPeriodService registrationPeriodService;

    public InternalPeriodController(RegistrationPeriodService registrationPeriodService) {
        this.registrationPeriodService = registrationPeriodService;
    }

    @GetMapping("/{periodId}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Integer periodId) {
        try {
            RegistrationPeriod p = registrationPeriodService.getPeriodById(periodId);

            Map<String, Object> data = new HashMap<>();
            data.put("periodId", p.getPeriodId());
            data.put("name", p.getPeriodName());
            data.put("startDate", p.getStartDate() == null ? null : p.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            data.put("endDate", p.getEndDate() == null ? null : p.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            data.put("status", p.getStatus() == null ? null : p.getStatus().name());

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("data", data);
            body.put("message", "OK");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }
    }
}


