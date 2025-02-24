package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.Rental;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RentalWithQuantityDTO {
    private final Rental rental;
    private final Integer quantityToReturn;
}
