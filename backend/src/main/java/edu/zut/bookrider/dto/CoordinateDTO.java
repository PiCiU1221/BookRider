package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateDTO {

    @NotBlank(message = "Latitude is required")
    @Pattern(regexp = "\\d{1,2}\\.\\d+", message = "Latitude must be in the format 'DD.DDD...'")
    private double latitude;

    @NotBlank(message = "Longitude is required")
    @Pattern(regexp = "\\d{1,2}\\.\\d+", message = "Longitude must be in the format 'DD.DDD...'")
    private double longitude;
}
