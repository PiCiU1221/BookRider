package edu.zut.bookrider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalReturnItemDTO {
    private Integer id;
    private Integer rentalId;
    private BookResponseDto book;
    private int returnedQuantity;
}
