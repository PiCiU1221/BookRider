package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.exception.BookNotFoundException;
import edu.zut.bookrider.exception.LibraryNotFoundException;
import edu.zut.bookrider.mapper.book.BookCreateEditMapper;
import edu.zut.bookrider.mapper.book.BookReadMapper;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.repository.BookRepository;
import edu.zut.bookrider.repository.LibraryRepository;
import edu.zut.bookrider.service.BookService;
import edu.zut.bookrider.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private LibraryRepository libraryRepository;
    @Mock
    private BookCreateEditMapper bookCreateEditMapper;
    @Mock
    private BookReadMapper bookReadMapper;
    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Library library;
    private BookRequestDto bookRequestDto;
    private BookResponseDto bookResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Address address = Address.builder()
                .street("Test Address.")
                .city("Test city")
                .postalCode("12345")
                .build();

        Category category = Category.builder()
                .name("Advantage")
                .build();

        Author author1 = Author.builder()
                .name("Test Author1")
                .build();

        Author author2 = Author.builder()
                .name("Test author2")
                .build();

        book = Book.builder()
                .title("Book Title")
                .releaseYear(2022)
                .build();

        library = Library.builder()
                .address(address)
                .name("Test Library")
                .phoneNumber("425-525-431")
                .email("library@gmail.com")
                .books(new ArrayList<>(Arrays.asList(book)))
                .build();

        bookRequestDto = new BookRequestDto(
                "Test Title",
                2022,
                category,
                List.of(author1, author2),
                List.of(library)
        );

        bookResponseDto = new BookResponseDto(
                1,
                "Test Title",
                2022,
                "Fiction",
                "http://image.url",
                List.of("author1")
        );
    }

    @Test
    void addNewBook_shouldAddBookSuccessfully() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(imageUploadService.uploadImage(image)).thenReturn("src/test/resources/imageUploadServiceTest/example_image.jpg");
        when(bookCreateEditMapper.map(bookRequestDto)).thenReturn(book);
        when(libraryRepository.findById(anyInt())).thenReturn(Optional.of(library));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookReadMapper.map(any(Book.class))).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.addNewBook(bookRequestDto, image, 1);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        verify(bookRepository).save(book);
        verify(libraryRepository).save(library);
    }

    @Test
    void addNewBook_shouldThrowLibraryNotFoundException_whenLibraryNotFound() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(imageUploadService.uploadImage(image)).thenReturn("src/test/resources/imageUploadServiceTest/example_image.jpg");
        when(bookCreateEditMapper.map(bookRequestDto)).thenReturn(book);
        when(libraryRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(LibraryNotFoundException.class, () -> bookService.addNewBook(bookRequestDto, image, 1));
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
