package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateLibrarianResponseDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String tempPassword;
}
