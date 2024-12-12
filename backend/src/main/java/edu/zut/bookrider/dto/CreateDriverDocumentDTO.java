package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverDocumentDTO {
    private byte[] imageInBytes;
    private String documentType;
    private LocalDate expiryDate;
}
