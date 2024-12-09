package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibrarianLoginRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "LibraryId is required")
    private Integer libraryId;

    @NotBlank(message = "Password is required")
    private String password;
}
