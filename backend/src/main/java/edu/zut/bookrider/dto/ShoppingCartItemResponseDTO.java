package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartItemResponseDTO {
    private String libraryName;
    private BigDecimal totalItemDeliveryCost;
    private List<ShoppingCartSubItemResponseDTO> books;
}
