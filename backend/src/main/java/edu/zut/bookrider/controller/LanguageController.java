package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.service.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<List<FilterResponseDTO>> getLanguages() {
        return ResponseEntity.ok(languageService.getAllLanguages());
    }
}
