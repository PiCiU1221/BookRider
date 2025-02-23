package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalReturnPriceResponseDTO {
    private BigDecimal totalPrice;
    private BigDecimal deliveryCost;
    private BigDecimal totalLateFees;
    private List<RentalLateFeeDTO> lateFees;
}
