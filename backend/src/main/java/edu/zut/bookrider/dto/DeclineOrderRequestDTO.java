package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclineOrderRequestDTO {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
