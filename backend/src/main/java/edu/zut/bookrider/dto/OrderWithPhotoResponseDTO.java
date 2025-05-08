package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithPhotoResponseDTO {
    private Integer orderId;
    private String userId;
    private String libraryName;
    private String pickupAddress;
    private String destinationAddress;
    private Boolean isReturn;
    private String status;
    private BigDecimal amount;
    private String paymentStatus;
    private String noteToDriver;
    private String deliveryPhotoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime driverAssignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private List<OrderItemResponseDTO> orderItems;
}
