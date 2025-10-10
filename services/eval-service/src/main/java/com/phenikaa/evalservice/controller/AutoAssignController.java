package com.phenikaa.evalservice.controller;

import com.phenikaa.evalservice.dto.*;
import com.phenikaa.evalservice.dto.request.ConfirmAutoAssignRequest;
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
    public ResponseEntity<AutoAssignPreviewResponse> preview(
            @RequestParam(value = "mode", required = false) String mode,
            @RequestBody AutoAssignPreviewRequest req) {
        if (mode != null && "gemini".equalsIgnoreCase(mode)) {
            return ResponseEntity.ok(autoAssignService.previewWithGemini(req));
        }
        return ResponseEntity.ok(autoAssignService.preview(req));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ConfirmAutoAssignResponse> confirm(@RequestBody ConfirmAutoAssignRequest req) {
        return ResponseEntity.ok(autoAssignService.confirm(req));
    }
}


