package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.AttributeAddRequestDto;
import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/search")
    public ResponseEntity<List<FilterResponseDTO>> searchAuthors(@RequestParam("name") String name) {
        Pageable pageable = PageRequest.of(0, 5);

        List<FilterResponseDTO> authors = authorService.searchAuthors(name, pageable);

        return ResponseEntity.ok(authors);
    }

    @PreAuthorize("hasRole('librarian')")
    @PostMapping
    public ResponseEntity<FilterResponseDTO> addAuthor(
            @RequestBody @Valid AttributeAddRequestDto attributeAddRequestDto) {

        FilterResponseDTO addedAuthor = authorService.addAuthor(attributeAddRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedAuthor);
    }
}
