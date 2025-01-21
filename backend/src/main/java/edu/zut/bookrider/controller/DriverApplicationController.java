package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import edu.zut.bookrider.service.DriverApplicationRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driver-applications")
@CrossOrigin
public class DriverApplicationController {

    private final DriverApplicationRequestService driverApplicationRequestService;

    @PreAuthorize("hasRole('driver')")
    @PostMapping
    public ResponseEntity<?> createDriverApplication(
            @RequestBody List<@Valid CreateDriverDocumentStringDTO> files,
            Authentication authentication
    ) {
        List<CreateDriverDocumentDTO> documents = new ArrayList<>();

        for (CreateDriverDocumentStringDTO documentDTO : files) {
            byte[] decodedBytes = Base64.getDecoder().decode(documentDTO.getBase64Image());
            String documentType = documentDTO.getDocumentType();
            LocalDate expirationDate = LocalDate.parse(documentDTO.getExpirationDate(), DateTimeFormatter.ISO_DATE);

            CreateDriverDocumentDTO document = new CreateDriverDocumentDTO();
            document.setImageInBytes(decodedBytes);
            document.setDocumentType(documentType);
            document.setExpiryDate(expirationDate);

            documents.add(document);
        }

        CreateDriverApplicationResponseDTO responseDTO = driverApplicationRequestService.createDriverApplication(authentication, documents);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('system_administrator')")
    public ResponseEntity<List<DriverApplicationSummaryDTO>> listApplications(
            @RequestParam(required = false) List<DriverApplicationStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(driverApplicationRequestService.listApplications(statuses, page, size));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('driver')")
    public ResponseEntity<List<DriverApplicationSummaryDTO>> listMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        return ResponseEntity.ok(driverApplicationRequestService.listUserApplications(page, size, authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('system_administrator', 'driver')")
    public ResponseEntity<DriverApplicationDetailsDTO> getApplicationDetails(
            @PathVariable Integer id,
            Authentication authentication) {

        return ResponseEntity.ok(driverApplicationRequestService.getApplicationDetails(id, authentication));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('system_administrator')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Integer id,
            @RequestParam DriverApplicationStatus status,
            @RequestParam(required = false) String rejectionReason,
            Authentication authentication) {

        driverApplicationRequestService.changeStatus(id, status, rejectionReason, authentication);
        return ResponseEntity.noContent().build();
    }
}
