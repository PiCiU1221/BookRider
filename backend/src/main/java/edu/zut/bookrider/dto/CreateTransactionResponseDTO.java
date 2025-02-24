package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTransactionResponseDTO {
    private String userId;
    private Integer orderId;
    private BigDecimal amount;
    private String transactionType;
    private LocalDateTime transactionDate;
}
