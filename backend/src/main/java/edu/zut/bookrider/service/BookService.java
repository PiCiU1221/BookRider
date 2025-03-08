package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.dto.PageResponseDTO;
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
import java.util.stream.Collectors;

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
    public PageResponseDTO<BookResponseDto> searchBooks(
            String title,
            String library,
            String category,
            String language,
            String publisher,
            List<String> authorNames,
            Integer releaseYearFrom,
            Integer releaseYearTo,
            int page,
            int size,
            String sort) {

        Pageable pageable;

        if (sort == null || sort.isBlank()) {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        } else {
            pageable = createPageableWithSort(sort, page, size);
        }

        Page<Book> filteredBooksPage = bookRepository.findAllByFilters(
                title,
                library,
                category,
                language,
                publisher,
                authorNames,
                releaseYearFrom,
                releaseYearTo,
                pageable
        );

        List<BookResponseDto> bookDtos = filteredBooksPage.getContent().stream()
                .map(bookReadMapper::map)
                .toList();

        return new PageResponseDTO<>(
                bookDtos,
                filteredBooksPage.getNumber(),
                filteredBooksPage.getSize(),
                filteredBooksPage.getTotalElements(),
                filteredBooksPage.getTotalPages()
        );
    }

    private Pageable createPageableWithSort(String sort, int page, int size) {
        Sort sortObject;

        if ("title-asc".equalsIgnoreCase(sort)) {
            sortObject = Sort.by(Sort.Direction.ASC, "title");
        } else if ("title-desc".equalsIgnoreCase(sort)) {
            sortObject = Sort.by(Sort.Direction.DESC, "title");
        } else if ("release-year-asc".equalsIgnoreCase(sort)) {
            sortObject = Sort.by(Sort.Direction.ASC, "releaseYear");
        } else if ("release-year-desc".equalsIgnoreCase(sort)) {
            sortObject = Sort.by(Sort.Direction.DESC, "releaseYear");
        } else {
            sortObject = Sort.by(Sort.Direction.ASC, "title");
        }

        return PageRequest.of(page, size, sortObject);
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

        Publisher publisher = publisherRepository.findByName(bookRequestDto.getPublisher())
                .orElseThrow(() -> new PublisherNotFoundException("Publisher not found"));

        Language language = languageRepository.findByName(bookRequestDto.getLanguage())
                .orElseThrow(() -> new LanguageNotFoundException("Language not found"));

        List<Author> authors = new ArrayList<>();

        for (String authorName : bookRequestDto.getAuthors()) {
            Author author = authorRepository.findByName(authorName)
                    .orElseThrow(() -> new AuthorNotFoundException("Author '" + authorName + "' not found"));

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

        Publisher publisher = publisherRepository.findByName(bookRequestDto.getPublisher())
                .orElseThrow(() -> new PublisherNotFoundException("Publisher not found"));

        List<Author> authors = new ArrayList<>();

        for (String authorName : bookRequestDto.getAuthors()) {
            Author author = authorRepository.findByName(authorName)
                    .orElseThrow(() -> new AuthorNotFoundException("Author '" + authorName + "' not found"));

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

    public Book getBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }

    public List<FilterResponseDTO> searchBookTitles(String name, Pageable pageable) {
        Pageable sortedByTitle = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("title").ascending());

        List<Book> bookTitles = bookRepository.findByTitleLike(name, sortedByTitle);

        return bookTitles.stream()
                .map(book -> new FilterResponseDTO(book.getTitle()))
                .collect(Collectors.toList());
    }
}
