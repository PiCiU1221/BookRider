package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverApplicationSummaryDTO {
    private Integer id;
    private String driverEmail;
    private DriverApplicationStatus status;
    private LocalDateTime submittedAt;
}
