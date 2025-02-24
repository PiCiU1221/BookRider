package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LibrarianDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
}
