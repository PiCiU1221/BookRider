package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverDocumentStringDTO {
    @NotBlank
    private String base64Image;
    @NotBlank
    private String documentType;
    @NotBlank
    private String expirationDate;
}
