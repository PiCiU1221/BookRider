package edu.zut.bookrider.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryCardDTO {

    @NotBlank(message = "User id is required")
    private String userId;

    @NotBlank(message = "Card id is required")
    private String cardId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Expiration date must be not null")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}
