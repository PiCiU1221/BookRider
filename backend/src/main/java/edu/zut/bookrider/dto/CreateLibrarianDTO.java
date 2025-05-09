package edu.zut.bookrider.dto;

import edu.zut.bookrider.validation.annotation.NotEmail;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLibrarianDTO {

    @NotNull(message = "Username cannot be null")
    @NotEmail
    private String username;

    @NotNull(message = "First name cannot be null")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    private String lastName;
}
