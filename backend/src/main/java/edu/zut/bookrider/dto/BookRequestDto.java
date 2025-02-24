package edu.zut.bookrider.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;


import java.util.List;

@Value
public class BookRequestDto {

    @NotEmpty(message = "Title cannot be empty")
    String title;

    @NotNull(message = "Category cannot be null")
    String categoryName;

    @NotEmpty(message = "AuthorIds list cannot be empty")
    List<Integer> authorIds;

    @NotNull(message = "Release year cannot be null")
    @Positive(message = "Release year must be a positive number")
    Integer releaseYear;

    @NotNull(message = "PublisherId cannot be null")
    Integer publisherId;

    @NotNull(message = "ISBN cannot be null")
    String isbn;

    @NotNull(message = "Language cannot be null")
    String language;

    @NotEmpty(message = "Image in byte64 format cannot be empty")
    String image;
}
