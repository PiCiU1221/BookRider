package edu.zut.bookrider.integration.controller;

import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<FilterResponseDTO>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
