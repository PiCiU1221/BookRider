package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.service.RentalReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rental-returns")
public class RentalReturnController {

    private final RentalReturnService rentalReturnService;

    @PreAuthorize("hasRole('librarian')")
    @GetMapping("/latest-by-driver/{driverId}")
    public ResponseEntity<RentalReturnDTO> getLatestReturnByDriver(@PathVariable String driverId) {

        RentalReturnDTO rentalReturn = rentalReturnService.getLatestReturnByDriver(driverId);
        return ResponseEntity.ok(rentalReturn);
    }

    @PreAuthorize("hasRole('librarian')")
    @PatchMapping("/{rentalReturnId}/complete-delivery")
    public ResponseEntity<Void> markDeliveryRentalReturnAsCompleted(@PathVariable Integer rentalReturnId) {

        rentalReturnService.markDeliveryReturnAsCompleted(rentalReturnId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('librarian')")
    @PatchMapping("/{rentalReturnId}/complete-in-person")
    public ResponseEntity<Void> markInPersonRentalReturnAsCompleted(@PathVariable Integer rentalReturnId) {

        rentalReturnService.markInPersonReturnAsCompleted(rentalReturnId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('user')")
    @PostMapping("/in-person")
    public ResponseEntity<List<RentalReturnDTO>> createInPersonReturn(@RequestBody @Valid InPersonRentalReturnRequestDTO inPersonRentalReturnRequestDTO) {

        List<RentalReturnDTO> rentalReturns = rentalReturnService.createInPersonRentalReturn(inPersonRentalReturnRequestDTO);
        return ResponseEntity.ok(rentalReturns);
    }

    @PreAuthorize("hasRole('user')")
    @PostMapping("/in-person/calculate-price")
    public ResponseEntity<RentalReturnPriceResponseDTO> calculateInPersonReturnPrice(@RequestBody @Valid InPersonRentalReturnRequestDTO inPersonRentalReturnRequestDTO) {

        RentalReturnPriceResponseDTO returnPrice = rentalReturnService.calculateInPersonReturnPrice(inPersonRentalReturnRequestDTO);
        return ResponseEntity.ok(returnPrice);
    }

    @PreAuthorize("hasRole('user')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<RentalReturnDTO>> getReturns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponseDTO<RentalReturnDTO> rentalReturns = rentalReturnService.getRentalReturns(pageable);

        return ResponseEntity.ok(rentalReturns);
    }

    @PreAuthorize("hasRole('user')")
    @PostMapping
    public ResponseEntity<List<RentalReturnDTO>> createRentalReturn(
            @RequestBody @Valid GeneralRentalReturnRequestDTO rentalReturnRequestDTO) {

        List<RentalReturnDTO> rentalReturns = rentalReturnService.createRentalReturn(rentalReturnRequestDTO);
        return ResponseEntity.ok(rentalReturns);
    }

    @PreAuthorize("hasRole('user')")
    @PostMapping("/calculate-price")
    public ResponseEntity<RentalReturnPriceResponseDTO> calculateReturnPrice(
            @RequestBody @Valid GeneralRentalReturnRequestDTO rentalReturnRequestDTO) {

        RentalReturnPriceResponseDTO returnPrice = rentalReturnService.calculateReturnPrice(rentalReturnRequestDTO);
        return ResponseEntity.ok(returnPrice);
    }

    @PreAuthorize("hasRole('user')")
    @PutMapping("/{rentalReturnId}/handover")
    public ResponseEntity<Void> confirmHandover(
            @PathVariable Integer rentalReturnId,
            @RequestParam String driverId) {

        rentalReturnService.confirmHandover(rentalReturnId, driverId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('librarian')")
    @GetMapping("/{rentalReturnId}")
    public ResponseEntity<RentalReturnDTO> getRentalReturnById(@PathVariable Integer rentalReturnId) {

        RentalReturnDTO rentalReturnDTO = rentalReturnService.getRentalReturnWithId(rentalReturnId);
        return ResponseEntity.ok(rentalReturnDTO);
    }

}
