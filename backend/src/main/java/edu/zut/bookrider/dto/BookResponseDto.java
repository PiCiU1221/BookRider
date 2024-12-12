package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.util.List;

@Value
public class BookResponseDto {

    @NotNull(message = "ID cannot be null")
    @Positive(message = "ID must be a positive number")
    Integer id;

    @NotNull(message = "Title cannot be null")
    String title;

    @NotNull(message = "Release year cannot be null")
    Integer releaseYear;

    @NotNull(message = "Category name cannot be null")
    String categoryName;

    String image;

    @NotNull(message = "Author names list cannot be null")
    List<String> authorNames;
}
