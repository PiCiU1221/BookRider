package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.LibraryCardDTO;
import edu.zut.bookrider.service.LibraryCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library-cards")
public class LibraryCardController {

    private final LibraryCardService libraryCardService;

    @PreAuthorize("hasRole('librarian')")
    @PostMapping
    public ResponseEntity<?> createLibraryCard(
            @Valid @RequestBody LibraryCardDTO libraryCardDTO) {

        LibraryCardDTO responseDto = libraryCardService.addLibraryCard(libraryCardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PreAuthorize("hasAnyRole('librarian', 'user')")
    @GetMapping("/{userId}")
    public ResponseEntity<List<LibraryCardDTO>> listLibraryCards(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        return ResponseEntity.ok(libraryCardService.getUsersLibraryCards(userId, page, size, authentication));
    }

    @PreAuthorize("hasRole('librarian')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLibraryCard(@PathVariable Integer id) {

        libraryCardService.deleteLibraryCard(id);
        return ResponseEntity.noContent().build();
    }
}
