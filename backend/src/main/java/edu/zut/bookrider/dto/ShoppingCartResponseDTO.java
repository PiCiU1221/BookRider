package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartResponseDTO {
    private BigDecimal totalCartDeliveryCost;
    private String deliveryAddress;
    private List<ShoppingCartItemResponseDTO> items;
}
