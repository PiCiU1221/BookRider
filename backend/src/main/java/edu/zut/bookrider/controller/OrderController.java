package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('user')")
    @GetMapping("/user/pending")
    public ResponseEntity<?> getPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponseDTO<UserOrderResponseDTO> response = orderService.getUserPendingOrders(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('user')")
    @GetMapping("/user/in-realization")
    public ResponseEntity<?> getInRealizationOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponseDTO<UserOrderResponseDTO> response = orderService.getUserInRealizationOrders(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('user')")
    @GetMapping("/user/completed")
    public ResponseEntity<?> getCompletedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponseDTO<UserOrderResponseDTO> response = orderService.getUserCompletedOrders(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('librarian')")
    @GetMapping("/librarian/pending")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getLibrarianPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<OrderResponseDTO> response = orderService.getLibraryPendingOrders(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('librarian')")
    @GetMapping("/librarian/in-realization")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getLibrarianInRealizationOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<OrderResponseDTO> response = orderService.getLibraryInRealizationOrders(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('librarian')")
    @GetMapping("/librarian/completed")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getLibrarianCompletedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<OrderResponseDTO> response = orderService.getLibraryCompletedOrders(page, size);
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
    @PutMapping("/{orderId}/handover")
    public ResponseEntity<?> handOverToDriver(
            @PathVariable Integer orderId,
            @RequestParam String driverId) {

        orderService.handoverOrderToDriver(orderId, driverId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('driver')")
    @GetMapping("/driver/pending")
    public ResponseEntity<?> getDriverPendingOrders(
            @RequestParam @NotBlank @Pattern(regexp = "^[-+]?\\d*\\.\\d+,[-+]?\\d*\\.\\d+$", message = "Start coordinates must be in 'double,double' format") String locationString,
            @RequestParam double maxDistanceInMeters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String[] startParsed = locationString.split(",");
        double locationLatitude = Double.parseDouble(startParsed[0]);
        double locationLongitude = Double.parseDouble(startParsed[1]);

        CoordinateDTO location = new CoordinateDTO(locationLatitude, locationLongitude);

        PageResponseDTO<OrderResponseDTO> response = orderService.getDriverPendingOrdersWithDistance(
                location, maxDistanceInMeters, page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('driver')")
    @GetMapping("/driver/in-realization")
    public ResponseEntity<?> getDriverInRealizationOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<OrderResponseDTO> response = orderService.getDriverInRealizationOrders(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('driver')")
    @GetMapping("/driver/completed")
    public ResponseEntity<?> getDriverCompletedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<OrderWithPhotoResponseDTO> response = orderService.getDriverCompletedOrders(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('driver')")
    @PutMapping("/{orderId}/assign")
    public ResponseEntity<?> assignDriverToOrder(
            @PathVariable Integer orderId) {
        orderService.assignDriverToOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('driver')")
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<DeliverOrderResponseDTO> deliverOrder(
            @PathVariable Integer orderId,
            @Valid @RequestBody DeliverOrderRequestDTO request) {

        DeliverOrderResponseDTO response = orderService.deliverOrder(orderId, request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('driver')")
    @PostMapping("/{orderId}/pickup-navigation")
    public ResponseEntity<?> pickupNavigation(
            @PathVariable Integer orderId,
            @Valid @RequestBody DeliveryNavigationRequestDTO navigationRequestDTO) {

        NavigationResponseDTO response = orderService.getNavigation(
                orderId,
                navigationRequestDTO,
                true
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('driver')")
    @PostMapping("/{orderId}/delivery-navigation")
    public ResponseEntity<?> getDeliveryNavigation(
            @PathVariable Integer orderId,
            @Valid @RequestBody DeliveryNavigationRequestDTO navigationRequestDTO) {

        NavigationResponseDTO response = orderService.getNavigation(
                orderId,
                navigationRequestDTO,
                false
        );
        return ResponseEntity.ok(response);
    }
}
