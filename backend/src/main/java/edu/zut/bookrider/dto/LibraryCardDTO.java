package edu.zut.bookrider.dto;

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

    private String userId;
    private String cardId;
    private String firstName;
    private String lastName;
    private LocalDate expirationDate;
}
