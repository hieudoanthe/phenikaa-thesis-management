package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.*;
import com.phenikaa.evalservice.service.AutoAssignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eval-service/admin/auto-assign")
@RequiredArgsConstructor
public class AutoAssignController {

    private final AutoAssignService autoAssignService;

    @PostMapping("/preview")
    public ResponseEntity<AutoAssignPreviewResponse> preview(@RequestBody AutoAssignPreviewRequest req) {
        return ResponseEntity.ok(autoAssignService.preview(req));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ConfirmAutoAssignResponse> confirm(@RequestBody ConfirmAutoAssignRequest req) {
        return ResponseEntity.ok(autoAssignService.confirm(req));
    }
}


