package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.QuoteResponseDTO;
import edu.zut.bookrider.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    @PreAuthorize("hasRole('user')")
    @PostMapping
    public ResponseEntity<?> createQuote(
            @RequestParam Integer bookId,
            @RequestParam int quantity,
            Authentication authentication) {

        QuoteResponseDTO quoteResponseDTO = quoteService.generateQuote(bookId, quantity, authentication);
        return ResponseEntity.ok(quoteResponseDTO);
    }
}
