package edu.zut.bookrider.mapper.book;

import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Author;
import edu.zut.bookrider.model.Book;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BookReadMapper implements Mapper<Book, BookResponseDto> {
    @Override
    public BookResponseDto map(Book object) {
        return new BookResponseDto(
                object.getId(),
                object.getTitle(),
                object.getCategory() != null ? object.getCategory().getName() : null,
                object.getAuthors().stream()
                        .map(Author::getName)
                        .collect(Collectors.toList()),
                object.getReleaseYear(),
                object.getPublisher() != null ? object.getPublisher().getName() : null,
                object.getIsbn(),
                object.getLanguage() != null ? object.getLanguage().getName() : null,
                object.getCoverImageUrl()

        );
    }
}
