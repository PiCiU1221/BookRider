package edu.zut.bookrider.mapper.book;

import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Book;
import org.springframework.stereotype.Component;

@Component
public class BookCreateEditMapper implements Mapper<BookRequestDto, Book> {

    @Override
    public Book map(BookRequestDto fromObject, Book toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    @Override
    public Book map(BookRequestDto object) {
        Book book = new Book();
        copy(object, book);
        return book;
    }

    private void copy(BookRequestDto object, Book book) {
        book.setTitle(object.getTitle());
        book.setReleaseYear(object.getReleaseYear());
        book.setCategory(object.getCategory());
        book.setAuthors(object.getAuthors());
        book.setLibraries(object.getLibraries());
    }
}
