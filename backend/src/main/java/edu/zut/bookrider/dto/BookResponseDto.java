package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseDto {

    @NotNull(message = "ID cannot be null")
    @Positive(message = "ID must be a positive number")
    Integer id;

    @NotNull(message = "Title cannot be null")
    String title;

    @NotNull(message = "Category name cannot be null")
    String categoryName;

    @NotNull(message = "Author names list cannot be null")
    List<String> authorNames;

    @NotNull(message = "Release year cannot be null")
    Integer releaseYear;

    @NotNull(message = "Publisher name cannot be null")
    String publisherName;

    @NotNull(message = "ISBN cannot be null")
    String isbn;

    @NotNull(message = "Language name cannot be null")
    String languageName;

    String image;
}
