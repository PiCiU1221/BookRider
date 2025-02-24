package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.CreateTransactionResponseDTO;
import edu.zut.bookrider.service.TransactionService;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<?> createDepositTransaction(
            @RequestParam @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,
            Authentication authentication) {

        CreateTransactionResponseDTO responseDTO = transactionService.createUserDepositTransaction(amount, authentication);
        return ResponseEntity.ok(responseDTO);
    }
}
