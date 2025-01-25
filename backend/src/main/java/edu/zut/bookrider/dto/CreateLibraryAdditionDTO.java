package edu.zut.bookrider.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLibraryAdditionDTO {

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "\\d{2}-\\d{3}", message = "Postal code must be in format XX-XXX")
    private String postalCode;

    @NotBlank(message = "Library name is required")
    private String libraryName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Library email is required")
    @Email(message = "Library email must be a valid email address")
    private String libraryEmail;
}
