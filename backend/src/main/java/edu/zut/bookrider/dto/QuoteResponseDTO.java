package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteResponseDTO {
    private LocalDateTime validUntil;
    private BookResponseDto book;
    private int quantity;
    private List<QuoteOptionResponseDTO> options;
}
