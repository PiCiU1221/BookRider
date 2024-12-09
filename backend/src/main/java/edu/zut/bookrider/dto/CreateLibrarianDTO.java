package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLibrarianDTO {
    private String username;
    private String firstName;
    private String lastName;
}
