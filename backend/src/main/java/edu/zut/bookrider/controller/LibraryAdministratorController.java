package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.CreateLibrarianDTO;
import edu.zut.bookrider.dto.CreateLibrarianResponseDTO;
import edu.zut.bookrider.security.AuthService;
import edu.zut.bookrider.service.LibraryAdministratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/library-admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('library_administrator')")
public class LibraryAdministratorController {

    private final LibraryAdministratorService libraryAdministratorService;
    private final AuthService authService;

    @GetMapping("/librarians")
    public ResponseEntity<?> getLibrarians(
            @RequestParam(required = false) String username,
            @RequestParam String libraryAdminEmail) {
        if (username != null) {
            return ResponseEntity.ok(libraryAdministratorService.findLibrarianByUsername(username, libraryAdminEmail));
        }
        return ResponseEntity.ok(libraryAdministratorService.getAllLibrarians(libraryAdminEmail));
    }

    @PostMapping("/librarians")
    public ResponseEntity<?> addLibrarian(
            @RequestBody @Valid CreateLibrarianDTO createLibrarianDTO,
            @RequestParam String libraryAdminEmail) {
        CreateLibrarianResponseDTO addedLibrarian = authService.createLibrarian(createLibrarianDTO, libraryAdminEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedLibrarian);
    }

    @DeleteMapping("/librarians/{username}")
    public ResponseEntity<?> removeLibrarian(
            @PathVariable String username,
            @RequestParam String libraryAdminEmail) {
        libraryAdministratorService.deleteLibrarian(username, libraryAdminEmail);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/librarians/reset-password/{username}")
    public ResponseEntity<?> resetLibrarianPassword(
            @PathVariable String username,
            @RequestParam String newPassword,
            @RequestParam String libraryAdminEmail) {
        CreateLibrarianResponseDTO updatedLibrarian = libraryAdministratorService.resetLibrarianPassword(username, newPassword, libraryAdminEmail);
        return ResponseEntity.ok(updatedLibrarian);
    }
}
