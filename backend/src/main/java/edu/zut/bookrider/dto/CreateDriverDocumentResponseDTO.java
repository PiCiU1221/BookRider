package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDriverDocumentResponseDTO {
    private DocumentType documentType;
    private String documentPhotoUrl;
    private LocalDate expiryDate;
}
