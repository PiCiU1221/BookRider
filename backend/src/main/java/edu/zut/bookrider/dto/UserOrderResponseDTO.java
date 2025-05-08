package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderResponseDTO {
    private BigDecimal userPayment;
    private OrderResponseDTO orderResponseDTO;
}
