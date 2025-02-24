package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteOptionResponseDTO {
    private Integer quoteOptionId;
    private String libraryName;
    private BigDecimal distanceKm;
    private BigDecimal totalDeliveryCost;
}
