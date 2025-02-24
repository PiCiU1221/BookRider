package edu.zut.bookrider.mapper.publisher;

import edu.zut.bookrider.dto.PublisherResponseDto;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Book;
import edu.zut.bookrider.model.Publisher;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PublisherReadMapper implements Mapper<Publisher, PublisherResponseDto> {

    @Override
    public PublisherResponseDto map(Publisher object) {
        return new PublisherResponseDto(
                object.getId(),
                object.getName(),
                object.getBooks().stream()
                        .map(Book::getTitle)
                        .collect(Collectors.toList())
        );
    }
}
