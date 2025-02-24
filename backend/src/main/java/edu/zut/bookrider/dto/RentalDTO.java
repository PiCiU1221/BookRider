package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentalDTO {
    private Integer rentalId;
    private BookResponseDto book;
    private String libraryName;
    private String libraryAddress;
    private Integer orderId;
    private int quantity;
    private LocalDateTime rentedAt;
    private LocalDateTime returnDeadline;
    private String status;
}
