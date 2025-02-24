package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartSubItemResponseDTO {
    private Integer subItemId;
    private BookResponseDto book;
    private int quantity;
}
