package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublisherRequestDto {

    @NotNull(message = "Publisher name cannot be null")
    String name;
}
