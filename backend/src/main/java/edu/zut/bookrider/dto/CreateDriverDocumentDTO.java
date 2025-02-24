package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.enums.DocumentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverDocumentDTO {

    @NotNull(message = "Image is required")
    private byte[] imageInBytes;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;
}
