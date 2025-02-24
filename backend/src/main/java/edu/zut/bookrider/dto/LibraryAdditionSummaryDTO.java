package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibraryAdditionSummaryDTO {
    private Integer id;
    private String creatorEmail;
    private String libraryName;
    private LibraryAdditionStatus status;
    private LocalDateTime submittedAt;
}
