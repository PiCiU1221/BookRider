package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.PageResponseDTO;
import edu.zut.bookrider.dto.RentalDTO;
import edu.zut.bookrider.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    @PreAuthorize("hasRole('user')")
    @GetMapping
    public ResponseEntity<PageResponseDTO<RentalDTO>> getUserRentals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rentedAt"));
        PageResponseDTO<RentalDTO> rentals = rentalService.getUserRentals(pageable);

        return ResponseEntity.ok(rentals);
    }
}
