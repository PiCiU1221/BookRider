package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import edu.zut.bookrider.service.LibraryAdditionRequestService;
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
@RequestMapping("/api/library-requests")
public class LibraryAdditionRequestController {

    private final LibraryAdditionRequestService libraryAdditionRequestService;

    @PreAuthorize("hasRole('library_administrator')")
    @PostMapping
    public ResponseEntity<?> createLibraryRequest(
            @RequestBody @Valid CreateLibraryAdditionDTO createLibraryAdditionDTO,
            Authentication authentication
            ) {
        CreateLibraryAdditionResponseDTO responseDTO = libraryAdditionRequestService.createLibraryRequest(createLibraryAdditionDTO, authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('system_administrator')")
    public ResponseEntity<List<LibraryAdditionSummaryDTO>> listRequests(
            @RequestParam(required = false) List<LibraryAdditionStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(libraryAdditionRequestService.listRequests(statuses, page, size));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('driver')")
    public ResponseEntity<List<LibraryAdditionSummaryDTO>> listMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        return ResponseEntity.ok(libraryAdditionRequestService.listUserRequests(page, size, authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('system_administrator', 'driver')")
    public ResponseEntity<LibraryRequestDetailsDTO> getApplicationDetails(
            @PathVariable Integer id,
            Authentication authentication) {

        return ResponseEntity.ok(libraryAdditionRequestService.getRequestDetails(id, authentication));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('system_administrator')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Integer id,
            @RequestParam LibraryAdditionStatus status,
            @RequestParam(required = false) String rejectionReason,
            Authentication authentication) {

        libraryAdditionRequestService.changeStatus(id, status, rejectionReason, authentication);
        return ResponseEntity.noContent().build();
    }
}
