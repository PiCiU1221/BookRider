package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.DeclineOrderRequestDTO;
import edu.zut.bookrider.dto.OrdersResponseDTO;
import edu.zut.bookrider.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('user')")
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(Authentication authentication) {

        OrdersResponseDTO response = orderService.getUserOrders(authentication);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('librarian')")
    @GetMapping("/librarian")
    public ResponseEntity<?> getLibrarianOrders(Authentication authentication) {

        OrdersResponseDTO response = orderService.getLibraryOrders(authentication);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('librarian')")
    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<?> acceptOrder(@PathVariable Integer orderId) {

        orderService.acceptOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('librarian')")
    @PatchMapping("/{orderId}/decline")
    public ResponseEntity<?> declineOrder(
            @PathVariable Integer orderId,
            @RequestBody @Valid DeclineOrderRequestDTO declineOrderRequestDTO) {

        orderService.declineOrder(orderId, declineOrderRequestDTO);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('librarian')")
    @PostMapping("/{orderId}/handover")
    public ResponseEntity<?> handOverToDriver(
            @PathVariable Integer orderId,
            @RequestParam String driverId) {

        orderService.handoverOrderToDriver(orderId, driverId);
        return ResponseEntity.ok().build();
    }
}
