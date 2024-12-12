package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.exception.BookNotFoundException;
import edu.zut.bookrider.exception.LibraryNotFoundException;
import edu.zut.bookrider.mapper.book.BookCreateEditMapper;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.Book;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.repository.BookRepository;
import edu.zut.bookrider.repository.LibraryRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;
    private final BookCreateEditMapper bookCreateEditMapper;
    private final BookReadMapper bookReadMapper;
    private final ImageUploadService imageUploadService;

    @Transactional(readOnly = true)
    public List<BookResponseDto> getFilteredBooks(Integer libraryId, Integer categoryId,
                                                  String authorName, String releaseYear, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releaseYear"));
        Page<Book> filteredBooksPage = bookRepository.findAllByFilters(libraryId, categoryId, authorName, releaseYear, pageable);

        return filteredBooksPage.getContent().stream()
                .map(bookReadMapper::map)
                .toList();
    }

    @Transactional
    public BookResponseDto addNewBook(@Valid BookRequestDto bookRequestDto, MultipartFile image, Integer libraryId) throws IOException {
        String coverImageUrl = imageUploadService.uploadImage(image);

        Book book = bookCreateEditMapper.map(bookRequestDto);

        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new LibraryNotFoundException("Library not found"));

        book.setCoverImageUrl(coverImageUrl);
        book = bookRepository.save(book);

        library.getBooks().add(book);
        libraryRepository.save(library);

        return bookReadMapper.map(book);
    }

    public BookResponseDto findBookById(Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book with id " + bookId + " not found"));
        return bookReadMapper.map(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> booksPage = bookRepository.findAll(pageable);

        return booksPage.getContent().stream()
                .map(bookReadMapper::map)
                .toList();
    }

    @Transactional
    public BookResponseDto updateBook(Integer bookId, @Valid BookRequestDto bookRequestDto) throws IOException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book with id " + bookId + " not found"));

        book.setTitle(bookRequestDto.getTitle());
        book.setAuthors(bookRequestDto.getAuthors());
        book.setCategory(bookRequestDto.getCategory());
        book.setReleaseYear(bookRequestDto.getReleaseYear());

        book = bookRepository.save(book);
        return bookReadMapper.map(book);
    }

    @Transactional
    public void deleteBook(Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book with id " + bookId + " not found"));

        book.getLibraries().forEach(library -> library.getBooks().remove(book));
        libraryRepository.saveAll(book.getLibraries());

        bookRepository.delete(book);
    }

    @Transactional
    public void addExistingBookToLibrary(Integer bookId, Integer libraryId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book with id " + bookId + " not found"));
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new LibraryNotFoundException("Library with id " + libraryId + " not found"));

        library.getBooks().add(book);
        libraryRepository.save(library);
    }
}
