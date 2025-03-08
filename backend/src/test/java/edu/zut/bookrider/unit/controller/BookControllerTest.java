package edu.zut.bookrider.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.controller.BookController;
import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.dto.PageResponseDTO;
import edu.zut.bookrider.model.Author;
import edu.zut.bookrider.model.Category;
import edu.zut.bookrider.model.Language;
import edu.zut.bookrider.model.Publisher;
import edu.zut.bookrider.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;

    private BookRequestDto bookRequestDto;
    private BookResponseDto bookResponseDto;
    private Author author;
    private Category category;
    private Publisher publisher;
    private Language language;

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

        publisher = Publisher.builder()
                .name("Publisher")
                .build();
        publisher.setId(1);

        language = Language.builder()
                .name("Language")
                .build();
        language.setId(1);

        bookRequestDto = new BookRequestDto(
                "Test Title",
                category.getName(),
                List.of(author.getName()),
                2022,
                publisher.getName(),
                "1234567891234",
                language.getName(),
                "imageByte64String"
        );

        bookResponseDto = new BookResponseDto(
                1,
                "Test Title",
                "Fiction",
                List.of("Author Name"),
                2022,
                "Publisher",
                "1234567891234",
                "Language",
                "http://image.url"
        );
    }

    @Test
    void searchBooks_shouldReturnFilteredBooks() throws Exception {
        List<BookResponseDto> bookList = Collections.singletonList(bookResponseDto);

        PageResponseDTO<BookResponseDto> booksResponse = new PageResponseDTO<>(bookList, 0, 10, bookList.size(), 1);
        when(bookService.searchBooks(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any())).thenReturn(booksResponse);

        mockMvc.perform(get("/api/books/search")
                        .param("authorName", "Author Name")
                        .param("releaseYearFrom", "2022")
                        .param("releaseYearTo", "2022")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Title"))
                .andExpect(jsonPath("$.content[0].authorNames[0]").value("Author Name"));

        verify(bookService).searchBooks(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any());
    }

    @Test
    void getAllBooks_shouldReturnAllBooks() throws Exception {
        List<BookResponseDto> books = Collections.singletonList(bookResponseDto);

        when(bookService.getAllBooks(Mockito.anyInt(), Mockito.anyInt())).thenReturn(books);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].authorNames[0]").value("Author Name"));

        verify(bookService).getAllBooks(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void addNewBook_shouldReturnAddedBook() throws Exception {

        when(bookService.addNewBook(any(), any())).thenReturn(bookResponseDto);

        String jsonBody = objectMapper.writeValueAsString(bookRequestDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.authorNames[0]").value("Author Name"));

        verify(bookService).addNewBook(any(), any());
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
                .andExpect(jsonPath("$.title").value("Test Title"))
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

        Author updatedAuthor = Author.builder()
                .name("Updated Author")
                .build();
        updatedAuthor.setId(2);

        Category updatedCategory = Category.builder()
                .name("Updated Category")
                .build();
        updatedCategory.setId(2);

        Publisher updatedPublisher = Publisher.builder()
                .name("Updated Publisher")
                .build();
        updatedPublisher.setId(2);

        Language updatedLanguage = Language.builder()
                .name("Updated Language")
                .build();
        updatedPublisher.setId(2);

        BookRequestDto bookPutRequestDto = new BookRequestDto(
                "Updated Book Title",
                updatedCategory.getName(),
                List.of(author.getName()),
                2023,
                updatedPublisher.getName(),
                "09871231231233",
                updatedLanguage.getName(),
                "updatedImageByte64"
        );

        String jsonBody = objectMapper.writeValueAsString(bookRequestDto);

        mockMvc.perform(put("/api/books/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
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
