package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.CreateOrderResponseDTO;
import edu.zut.bookrider.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<?> checkout(Authentication authentication) {

        List<CreateOrderResponseDTO> orders = checkoutService.checkout(authentication);
        return ResponseEntity.ok(orders);
    }
}
