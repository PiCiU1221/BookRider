package edu.zut.bookrider.unit.controller;

import edu.zut.bookrider.controller.BookController;
import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.model.Author;
import edu.zut.bookrider.model.Category;
import edu.zut.bookrider.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;

    private BookRequestDto bookRequestDto;
    private BookResponseDto bookResponseDto;
    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();

        author = Author.builder()
                .name("Author Name")
                .build();
        author.setId(1);
        category = Category.builder()
                .name("Advantage")
                .build();
        category.setId(1);

        bookRequestDto = new BookRequestDto(
                "Book Title",
                2022,
                category,
                Collections.singletonList(author),
                Collections.emptyList()
        );

        bookResponseDto = new BookResponseDto(
                1,
                "Book Title",
                2022,
                category.getName(),
                "image_url",
                Collections.singletonList(author.getName())
        );
    }

    @Test
    void getFilteredBooks_shouldReturnFilteredBooks() throws Exception {
        List<BookResponseDto> books = Collections.singletonList(bookResponseDto);

        when(bookService.getFilteredBooks(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(books);

        mockMvc.perform(get("/api/books/filtered")
                        .param("authorName", "Author Name")
                        .param("releaseYear", "2022")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Book Title"))
                .andExpect(jsonPath("$[0].authorNames[0]").value("Author Name"));

        verify(bookService).getFilteredBooks(any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void getAllBooks_shouldReturnAllBooks() throws Exception {
        List<BookResponseDto> books = Collections.singletonList(bookResponseDto);

        when(bookService.getAllBooks(Mockito.anyInt(), Mockito.anyInt())).thenReturn(books);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Book Title"))
                .andExpect(jsonPath("$[0].authorNames[0]").value("Author Name"));

        verify(bookService).getAllBooks(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void addNewBook_shouldReturnAddedBook() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", new byte[0]);

        Path bookRequestFilePath = Path.of("src/test/resources/bookServiceTest/valid-book-response.json");
        byte[] bookRequestBytes = Files.readAllBytes(bookRequestFilePath);

        MockMultipartFile bookRequestDtoPart = new MockMultipartFile(
                "bookRequestDto",
                "valid-book-response.json",
                "application/json",
                bookRequestBytes
        );

        when(bookService.addNewBook(any(), any(), anyInt())).thenReturn(bookResponseDto);

        mockMvc.perform(multipart("/api/books/add")
                        .file(image)
                        .file(bookRequestDtoPart)
                        .param("id", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book Title"))
                .andExpect(jsonPath("$.authorNames[0]").value("Author Name"));

        verify(bookService).addNewBook(any(), any(), anyInt());
    }

    @Test
    void addExistingBookToLibrary_shouldReturnNoContent() throws Exception {
        doNothing().when(bookService).addExistingBookToLibrary(anyInt(), anyInt());

        mockMvc.perform(post("/api/books/add-existing/{bookId}", 1)
                        .param("libraryId", "2"))
                .andExpect(status().isNoContent());

        verify(bookService).addExistingBookToLibrary(1, 2);
    }

    @Test
    void getBookById_shouldReturnBook() throws Exception {
        when(bookService.findBookById(1)).thenReturn(bookResponseDto);

        mockMvc.perform(get("/api/books/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book Title"))
                .andExpect(jsonPath("$.authorNames[0]").value("Author Name"));

        verify(bookService).findBookById(1);
    }

    @Test
    public void updateBook_shouldReturnUpdatedBookSuccessfully() throws Exception {
        BookResponseDto bookResponseDto = mock(BookResponseDto.class);
        when(bookResponseDto.getTitle()).thenReturn("Updated Book Title");
        when(bookResponseDto.getReleaseYear()).thenReturn(2023);
        when(bookResponseDto.getCategoryName()).thenReturn("Updated Category");
        when(bookResponseDto.getAuthorNames()).thenReturn(Collections.singletonList("Updated Author"));
        when(bookResponseDto.getImage()).thenReturn("image_url");
        when(bookService.updateBook(eq(1), any(BookRequestDto.class))).thenReturn(bookResponseDto);

        String bookRequestJson = "{\"title\":\"Updated Book Title\",\"year\":2023,\"category\":{\"name\":\"Updated Category\"},\"authors\":[{\"name\":\"Updated Author\"}],\"libraries\":[{\"name\":\"Library Name\"}],\"releaseYear\":2023}";

        mockMvc.perform(put("/api/books/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Book Title"))
                .andExpect(jsonPath("$.releaseYear").value(2023))
                .andExpect(jsonPath("$.categoryName").value("Updated Category"))
                .andExpect(jsonPath("$.authorNames[0]").value("Updated Author"))
                .andExpect(jsonPath("$.image").value("image_url"));

        verify(bookService, times(1)).updateBook(eq(1), any(BookRequestDto.class));
    }

    @Test
    void deleteBook_shouldReturnNoContent() throws Exception {
        doNothing().when(bookService).deleteBook(1);

        mockMvc.perform(delete("/api/books/{id}", 1))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1);
    }
}
