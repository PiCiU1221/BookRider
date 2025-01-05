package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponseDTO {
    private String userId;
    private String libraryName;
    private String deliveryAddress;
    private String status;
    private BigDecimal amount;
    private String paymentStatus;
    private String noteToDriver;
    private List<CreateOrderItemResponseDTO> orderItems;
}
