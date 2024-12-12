package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLibraryAdditionDTO {
    private String street;
    private String city;
    private String postalCode;
    private String libraryName;
    private String phoneNumber;
    private String libraryEmail;
}
