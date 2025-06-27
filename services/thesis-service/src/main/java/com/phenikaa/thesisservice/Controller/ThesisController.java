package com.phenikaa.thesisservice.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/thesis-service/api/thesis")
@CrossOrigin(origins = "*")
public class ThesisController {

    @GetMapping("/all")
    public List<Map<String, String>> getAll() {
        return List.of(
                Map.of("id", "1", "title", "Luận văn A", "author", "Nguyễn Văn A"),
                Map.of("id", "2", "title", "Luận văn B", "author", "Trần Thị B")
        );
    }
}

