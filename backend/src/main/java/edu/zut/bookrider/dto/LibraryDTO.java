package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryDTO {
    private Integer id;
    private CreateAddressDTO address;
    private String name;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
}
