package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibraryRequestDetailsDTO {
    private Integer id;
    private String creatorEmail;
    private String reviewerId;
    private Address address;
    private String libraryName;
    private String phoneNumber;
    private String libraryEmail;
    private LibraryAdditionStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
}
