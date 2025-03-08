package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.AttributeAddRequestDto;
import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("hasRole('librarian')")
    @PostMapping
    public ResponseEntity<FilterResponseDTO> addLanguage(
            @RequestBody @Valid AttributeAddRequestDto attributeAddRequestDto) {

        FilterResponseDTO addedLanguage = languageService.addLanguage(attributeAddRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedLanguage);
    }
}
