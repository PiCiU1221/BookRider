package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.dto.ShoppingCartResponseDTO;
import edu.zut.bookrider.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shopping-cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PreAuthorize("hasRole('user')")
    @PostMapping("/address")
    public ResponseEntity<?> setDeliveryAddress(
            @RequestBody CreateAddressDTO createAddressDTO,
            Authentication authentication) {

        ShoppingCartResponseDTO updatedShoppingCart = shoppingCartService.setDeliveryAddress(createAddressDTO, authentication);
        return ResponseEntity.ok(updatedShoppingCart);
    }

    @PreAuthorize("hasRole('user')")
    @PostMapping("/add-quote-option/{quoteOptionId}")
    public ResponseEntity<?> addQuoteOptionToCart(
            @PathVariable Integer quoteOptionId,
            Authentication authentication) {

        ShoppingCartResponseDTO updatedShoppingCart = shoppingCartService.addQuoteOptionToCart(quoteOptionId, authentication);
        return ResponseEntity.ok(updatedShoppingCart);
    }

    @PreAuthorize("hasRole('user')")
    @DeleteMapping("/delete-sub-item/{subItemId}")
    public ResponseEntity<?> deleteShoppingCartSubItem(
            @PathVariable Integer subItemId,
            Authentication authentication) {

        ShoppingCartResponseDTO updatedShoppingCart = shoppingCartService.deleteShoppingCartSubItem(subItemId, authentication);
        return ResponseEntity.ok(updatedShoppingCart);
    }

    @PreAuthorize("hasRole('user')")
    @GetMapping
    public ResponseEntity<ShoppingCartResponseDTO> getShoppingCart(Authentication authentication) {

        ShoppingCartResponseDTO shoppingCart = shoppingCartService.getShoppingCart(authentication);
        return ResponseEntity.ok(shoppingCart);
    }
}
