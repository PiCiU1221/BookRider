package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalReturnWithQuantityRequestDTO {

    @NotNull(message = "Rental ID is required")
    private Integer rentalId;

    @NotNull(message = "Quantity to return is required")
    @Positive(message = "Quantity to return must be greater than zero")
    private Integer quantityToReturn;
}
