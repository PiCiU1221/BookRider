package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.LibraryAdditionRequest;
import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLibraryAdditionResponseDTO {
    private Integer id;
    private String creatorEmail;
    private Address address;
    private String libraryName;
    private String phoneNumber;
    private String email;
    private LibraryAdditionStatus status;
}
