package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDriverDocumentResponseDTO {
    String documentType;
    String documentPhotoUrl;
    LocalDate expiryDate;
}
