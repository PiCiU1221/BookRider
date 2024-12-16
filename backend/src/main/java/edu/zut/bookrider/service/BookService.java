package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.exception.*;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.repository.*;
import edu.zut.bookrider.util.BASE64DecodedMultipartFile;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;
    private final BookReadMapper bookReadMapper;
    private final ImageUploadService imageUploadService;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final LanguageRepository languageRepository;

    @Transactional(readOnly = true)
    public List<BookResponseDto> getFilteredBooks(Integer libraryId, Integer categoryId,
                                                  String authorName, Integer releaseYearFrom, Integer releaseYearTo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> filteredBooksPage = bookRepository.findAllByFilters(libraryId, categoryId, authorName, releaseYearFrom, releaseYearTo, pageable);

        return filteredBooksPage.getContent().stream()
                .map(bookReadMapper::map)
                .toList();
    }

    @Transactional
    public BookResponseDto addNewBook(
            @Valid BookRequestDto bookRequestDto,
            Authentication authentication) {

        Integer libraryId = Integer.valueOf(authentication.getName().split(":")[1]);

        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new LibraryNotFoundException("Library not found"));

        Category category = categoryRepository.findByName(bookRequestDto.getCategoryName())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Publisher publisher = publisherRepository.findById(bookRequestDto.getPublisherId())
                .orElseThrow(() -> new PublisherNotFoundException("Publisher not found"));

        Language language = languageRepository.findByName(bookRequestDto.getLanguage())
                .orElseThrow(() -> new LanguageNotFoundException("Language not found"));

        List<Author> authors = new ArrayList<>();

        for (Integer authorId : bookRequestDto.getAuthorIds()) {
            Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new AuthorNotFoundException("Author not found"));

            authors.add(author);
        }

        byte[] imageBase64 = Base64.getDecoder().decode(bookRequestDto.getImage());
        MultipartFile multipartFile = new BASE64DecodedMultipartFile(imageBase64);
        String coverImageUrl;

        try {
            coverImageUrl = imageUploadService.uploadImage(multipartFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Book book = new Book();
        book.setTitle(bookRequestDto.getTitle());
        book.setReleaseYear(bookRequestDto.getReleaseYear());
        book.setCategory(category);
        book.setAuthors(authors);
        book.setPublisher(publisher);
        book.setCoverImageUrl(coverImageUrl);
        book.setLanguage(language);
        book.setIsbn(bookRequestDto.getIsbn());
        book.setCoverImageUrl(coverImageUrl);

        book = bookRepository.save(book);

        publisher.getBooks().add(book);
        publisherRepository.save(publisher);

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
    public BookResponseDto updateBook(Integer bookId, @Valid BookRequestDto bookRequestDto) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book with id " + bookId + " not found"));

        Category category = categoryRepository.findByName(bookRequestDto.getCategoryName())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Publisher publisher = publisherRepository.findById(bookRequestDto.getPublisherId())
                .orElseThrow(() -> new PublisherNotFoundException("Publisher not found"));

        List<Author> authors = new ArrayList<>();

        for (Integer authorId : bookRequestDto.getAuthorIds()) {
            Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new AuthorNotFoundException("Author not found"));

            authors.add(author);
        }

        byte[] imageBase64 = Base64.getDecoder().decode(bookRequestDto.getImage());
        MultipartFile multipartFile = new BASE64DecodedMultipartFile(imageBase64);
        String coverImageUrl;

        try {
            coverImageUrl = imageUploadService.uploadImage(multipartFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        book.setTitle(bookRequestDto.getTitle());
        book.setReleaseYear(bookRequestDto.getReleaseYear());
        book.setCategory(category);
        book.setAuthors(authors);
        book.setPublisher(publisher);
        book.setCoverImageUrl(coverImageUrl);

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
