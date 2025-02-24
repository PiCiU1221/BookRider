package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverApplicationResponseDTO {
    private Integer id;
    private String email;
    private String status;
    private List<CreateDriverDocumentResponseDTO> createdDocuments;
}
