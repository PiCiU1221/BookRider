package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLibrarianDTO {

    @NotNull(message = "Username cannot be null")
    @Pattern(regexp = "^[^@]+$", message = "Username cannot be an email")
    private String username;

    @NotNull(message = "First name cannot be null")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    private String lastName;
}
