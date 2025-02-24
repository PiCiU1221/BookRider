package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverOrderRequestDTO {

    @NotNull(message = "Location is required.")
    private CoordinateDTO location;
    @NotBlank(message = "Photo in Base64 format is required.")
    private String photoBase64;
}
