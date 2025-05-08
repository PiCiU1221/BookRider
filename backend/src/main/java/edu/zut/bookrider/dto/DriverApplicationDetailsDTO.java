package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverApplicationDetailsDTO {
    private Integer id;
    private String userEmail;
    private String reviewerID;
    private DriverApplicationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private List<CreateDriverDocumentResponseDTO> driverDocuments;
}
