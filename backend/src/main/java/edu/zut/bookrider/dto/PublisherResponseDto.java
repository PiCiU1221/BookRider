package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.util.List;

@Value
public class PublisherResponseDto {

    @NotNull(message = "ID cannot be null")
    @Positive(message = "ID must be a positive number")
    Integer id;

    @NotNull(message = "Publisher name cannot be null")
    String name;

    List<String> books;
}
