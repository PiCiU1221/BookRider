package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibrarianLoginRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "LibraryId is required")
    private Integer libraryId;

    @NotBlank(message = "Password is required")
    private String password;
}
