package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.exception.BookNotFoundException;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.repository.*;
import edu.zut.bookrider.service.BookService;
import edu.zut.bookrider.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.io.IOException;
import java.util.*;

public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookReadMapper bookReadMapper;
    @Mock
    private LibraryRepository libraryRepository;
    @Mock
    private ImageUploadService imageUploadService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private PublisherRepository publisherRepository;
    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Library library;
    private BookRequestDto bookRequestDto;
    private BookResponseDto bookResponseDto;
    private Category category;
    private Publisher publisher;
    private Author author1;
    private Author author2;
    private Language language;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Address address = Address.builder()
                .street("Test Address.")
                .city("Test city")
                .postalCode("12345")
                .build();

        category = Category.builder()
                .name("Advantage")
                .build();

        author1 = Author.builder()
                .name("Test Author1")
                .build();
        author1.setId(1);

        author2 = Author.builder()
                .name("Test author2")
                .build();
        author2.setId(2);

        publisher = Publisher.builder()
                .name("Publisher")
                .build();
        publisher.setId(1);

        language = Language.builder()
                .name("Language")
                .build();
        language.setId(1);

        book = Book.builder()
                .title("Test Title")
                .releaseYear(2022)
                .category(category)
                .authors(List.of(author1, author2))
                .publisher(publisher)
                .language(language)
                .build();

        library = Library.builder()
                .address(address)
                .name("Test Library")
                .phoneNumber("425-525-431")
                .email("library@gmail.com")
                .books(new ArrayList<>(Arrays.asList(book)))
                .build();

        String base64Image = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});

        bookRequestDto = new BookRequestDto(
                "Test Title",
                category.getName(),
                List.of(author1.getName(), author2.getName()),
                2022,
                publisher.getName(),
                "1234567891234",
                language.getName(),
                base64Image
        );

        bookResponseDto = new BookResponseDto(
                1,
                "Test Title",
                "Fiction",
                List.of("author1", "author2"),
                2022,
                "Publisher",
                "1234567891234",
                "Language",
                "http://image.url"
        );
    }

    @Test
    void addNewBook_shouldAddBookSuccessfully() throws IOException {
        when(imageUploadService.uploadImage(any())).thenReturn("src/test/resources/imageUploadServiceTest/example_image.jpg");
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookReadMapper.map(any(Book.class))).thenReturn(bookResponseDto);
        when(libraryRepository.findById(any())).thenReturn(Optional.of(library));
        when(categoryRepository.findByName(any())).thenReturn(Optional.of(category));
        when(publisherRepository.findByName(publisher.getName())).thenReturn(Optional.of(publisher));
        when(authorRepository.findByName(author1.getName())).thenReturn(Optional.of(author1));
        when(authorRepository.findByName(author2.getName())).thenReturn(Optional.of(author2));
        when(languageRepository.findByName(any())).thenReturn(Optional.of(language));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("librarian1:1");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        BookResponseDto result = bookService.addNewBook(bookRequestDto, authentication);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
    }

    @Test
    void findBookById_shouldReturnBookResponseDto() {
        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(book));
        when(bookReadMapper.map(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.findBookById(1);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
    }

    @Test
    void findBookById_shouldThrowBookNotFoundException_whenBookNotFound() {
        when(bookRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.findBookById(1));
    }

    @Test
    void getAllBooks_shouldReturnBooksList() {
        List<Book> books = List.of(book);
        Page<Book> booksPage = new PageImpl<>(books);
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(booksPage);
        when(bookReadMapper.map(any(Book.class))).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.getAllBooks(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Title", result.get(0).getTitle());
        verify(bookRepository).findAll(any(Pageable.class));
        verify(bookReadMapper, times(1)).map(any(Book.class));
    }

    @Test
    void updateBook_shouldUpdateBookSuccessfully() throws IOException {
        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookReadMapper.map(any(Book.class))).thenReturn(bookResponseDto);
        when(categoryRepository.findByName(any())).thenReturn(Optional.of(category));
        when(publisherRepository.findByName(publisher.getName())).thenReturn(Optional.of(publisher));
        when(authorRepository.findByName(author1.getName())).thenReturn(Optional.of(author1));
        when(authorRepository.findByName(author2.getName())).thenReturn(Optional.of(author2));

        BookResponseDto result = bookService.updateBook(1, bookRequestDto);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook_shouldThrowBookNotFoundException_whenBookNotFound() {
        when(bookRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(1, bookRequestDto));
    }

    @Test
    void deleteBook_shouldDeleteBookSuccessfully() {
        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(book));
        doNothing().when(bookRepository).delete(any(Book.class));

        bookService.deleteBook(1);

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_shouldThrowBookNotFoundException_whenBookNotFound() {
        when(bookRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(2));
    }
}
