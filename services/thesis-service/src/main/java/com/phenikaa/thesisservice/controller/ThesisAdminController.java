package com.phenikaa.thesisservice.controller;

import com.phenikaa.thesisservice.service.interfaces.ThesisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thesis-service/admin")
@RequiredArgsConstructor
public class ThesisAdminController {

    private final ThesisService thesisService;

}
