package edu.zut.bookrider.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InPersonRentalReturnRequestDTO {

    @NotNull(message = "Rental return request list is required")
    @Size(min = 1, message = "At least one rental return request is required")
    private List<@Valid RentalReturnWithQuantityRequestDTO> rentalReturnRequests;
}
