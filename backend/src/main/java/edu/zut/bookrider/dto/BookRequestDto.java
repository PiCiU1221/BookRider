package edu.zut.bookrider.dto;

import edu.zut.bookrider.model.Author;
import edu.zut.bookrider.model.Category;
import edu.zut.bookrider.model.Library;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;


import java.util.List;

@Value
public class BookRequestDto {

    @NotEmpty(message = "Title cannot be empty")
    String title;

    @NotNull(message = "Release year cannot be null")
    @Positive(message = "Release year must be a positive number")
    Integer releaseYear;

    @NotNull(message = "Category cannot be null")
    Category category;

    @NotEmpty(message = "Authors list cannot be empty")
    List<Author> authors;

    @NotEmpty(message = "Libraries list cannot be empty")
    List<Library> libraries;

}
