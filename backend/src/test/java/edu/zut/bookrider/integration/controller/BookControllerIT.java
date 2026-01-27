package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.repository.*;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class BookControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserIdGeneratorService userIdGeneratorService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private LanguageRepository languageRepository;
    @Autowired
    private PublisherRepository publisherRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AddressRepository addressRepository;

    Library libraryReference;
    Library bookLibrary;

    @BeforeEach
    void setUp() {
        libraryReference = libraryRepository.findById(1).orElseThrow();
        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();

        User librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setUsername("librarian67");
        librarian.setRole(librarianRole);
        librarian.setLibrary(libraryReference);
        librarian.setPassword(passwordEncoder.encode("password"));
        userRepository.save(librarian);

        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("bcit_user@bookrider.pl");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(userRole);
        userRepository.save(user);

        Address address = new Address();
        address.setCity("something");
        address.setPostalCode("something");
        address.setStreet("something");
        address.setLatitude(BigDecimal.valueOf(10.0));
        address.setLongitude(BigDecimal.valueOf(10.0));
        Address savedAddress = addressRepository.save(address);

        Library library = new Library();
        library.setAddress(savedAddress);
        library.setName("bcit_library");
        bookLibrary = libraryRepository.save(library);
    }

    private Category createCategory(String categoryName) {
        Optional<Category> optionalCategory = categoryRepository.findByName(categoryName);

        if (optionalCategory.isPresent()) {
            return optionalCategory.get();
        } else {
            Category category = new Category();
            category.setName(categoryName);
            return categoryRepository.save(category);
        }
    }

    private Language createLanguage(String languageName) {
        Optional<Language> optionalLanguage = languageRepository.findByName(languageName);

        if (optionalLanguage.isPresent()) {
            return optionalLanguage.get();
        } else {
            Language language = new Language();
            language.setName(languageName);
            return languageRepository.save(language);
        }
    }

    private Publisher createPublisher(String publisherName) {
        Optional<Publisher> optionalPublisher = publisherRepository.findByName(publisherName);

        if (optionalPublisher.isPresent()) {
            return optionalPublisher.get();
        } else {
            Publisher publisher = new Publisher();
            publisher.setName(publisherName);
            return publisherRepository.save(publisher);
        }
    }

    private List<Author> createAuthors(List<String> authorNames) {
        List<Author> authors = new ArrayList<>();

        for (String authorName : authorNames) {
            Optional<Author> optionalAuthor = authorRepository.findByName(authorName);

            if (optionalAuthor.isPresent()) {
                authors.add(optionalAuthor.get());
            } else {
                Author author = new Author();
                author.setName(authorName);
                authors.add(authorRepository.save(author));
            }
        }

        return authors;
    }

    private Book createBook(String category, String language, String publisher, List<String> authors, String title, String isbn, Integer releaseYear, String coverImageUrl) {
        Book book = new Book();
        book.setCategory(createCategory(category));
        book.setLanguage(createLanguage(language));
        book.setPublisher(createPublisher(publisher));
        book.setAuthors(createAuthors(authors));
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setReleaseYear(releaseYear);
        book.setCoverImageUrl(coverImageUrl);
        Book savedBook = bookRepository.save(book);

        List<Book> books = bookLibrary.getBooks();
        books.add(savedBook);
        bookLibrary.setBooks(books);
        libraryRepository.save(bookLibrary);

        return savedBook;
    }

    @Test
    @WithMockUser(username = "librarian67:1", roles = {"librarian"})
    void whenValidInput_returnDTO() throws Exception {
        File exampleImage = new ClassPathResource("test_image.jpg").getFile();
        String bookCoverString = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(exampleImage.toPath()));

        List<String> authors = new ArrayList<>(List.of("bcit_author1", "bcit_author2"));
        createAuthors(authors);
        createPublisher("bcit_publisher");


        BookRequestDto bookRequestDto = new BookRequestDto(
                "testTitle",
                "Poezja",
                authors,
                2024,
                "bcit_publisher",
                "1235437651243",
                "Polski",
                bookCoverString
        );

        String jsonBody = objectMapper.writeValueAsString(bookRequestDto);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);
    }

    @Test
    @WithMockUser(username = "bcit_user@bookrider.com:user", roles = {"user"})
    void whenSearchBooksWithZeroParameters_thenReturnAllBooks() throws Exception {
        List<String> authors1 = List.of("Walter White", "Jessie Pinkman");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book1", "04032025123", 2015, "random.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @WithMockUser(username = "bcit_user@bookrider.com:user", roles = {"user"})
    void whenSearchBooksWithTitleParameters_thenReturnCorrectBooks() throws Exception {
        List<String> authors1 = List.of("Walter White", "Jessie Pinkman");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book1", "04032025123", 2015, "random.com");

        List<String> authors2 = List.of("Walter White", "Jessie Pinkman");
        createBook("Poetry", "Polish", "Oxford", authors2, "BCIT_Book2", "04032025124", 2015, "random.com");

        List<String> authors3 = List.of("Walter White", "Jessie Pinkman");
        createBook("Poetry", "Polish", "Oxford", authors3, "BCIT_Book3", "04032025125", 2015, "random.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("title", "BCIT_")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(Matchers.equalTo(3)));
    }

    @Test
    @WithMockUser(username = "bcit_user@bookrider.com:user", roles = {"user"})
    void whenSearchBooksWithAllParameters_thenReturnCorrectBooks() throws Exception {
        List<String> authors1 = List.of("Walter White", "Jessie Pinkman");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book1", "04032025121", 2015, "random.com");

        // More authors (should be returned)
        List<String> authors2 = List.of("Walter White", "Jessie Pinkman", "Mike Tyson");
        createBook("Poetry", "Polish", "Oxford", authors2, "BCIT_Book2", "04032025122", 2015, "random.com");

        // Less authors and one match (shouldn't be returned)
        List<String> authors3 = List.of("Walter White");
        createBook("Poetry", "Polish", "Oxford", authors3, "BCIT_Book3", "04032025123", 2015, "random.com");

        // Wrong category
        createBook("Horror", "Polish", "Oxford", authors1, "BCIT_Book4", "04032025124", 2015, "random.com");

        // Wrong language
        createBook("Poetry", "German", "Oxford", authors1, "BCIT_Book5", "04032025125", 2015, "random.com");

        // Wrong publisher
        createBook("Poetry", "Polish", "Fortnight", authors1, "BCIT_Book6", "04032025126", 2015, "random.com");

        // Wrong authors
        List<String> authors4 = List.of("Hector Casablanca");
        createBook("Poetry", "Polish", "Oxford", authors4, "BCIT_Book7", "04032025127", 2015, "random.com");

        // Too old
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book8", "04032025128", 2000, "random.com");

        // Too young
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book9", "04032025129", 2025, "random.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("title", "BCIT_")
                        .param("library", "bcit_library")
                        .param("category", "Poetry")
                        .param("language", "Polish")
                        .param("publisher", "Oxford")
                        .param("authorNames", "Walter White")
                        .param("authorNames", "Jessie Pinkman")
                        .param("releaseYearFrom", "2010")
                        .param("releaseYearTo", "2020")
                        .param("sort", "title-desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(Matchers.equalTo(2)));
    }

    @Test
    @WithMockUser(username = "bcit_user@bookrider.com:user", roles = {"user"})
    void whenUserGetsTitlesWithNoInput_thenReturnAllTitles() throws Exception {

        List<String> authors1 = List.of("Walter White", "Jessie Pinkman");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book1", "04032025121", 2015, "random.com");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book2", "04032025122", 2015, "random.com");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book3", "04032025123", 2015, "random.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search-book-titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("title", "")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "bcit_user@bookrider.pl", roles = {"user"})
    void whenUserGetsTitlesWithInput_thenReturnOnlySelectedTitles() throws Exception {

        List<String> authors1 = List.of("Walter White", "Jessie Pinkman");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book1", "04032025121", 2015, "random.com");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book2", "04032025122", 2015, "random.com");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book3", "04032025123", 2015, "random.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search-book-titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("title", "BCIT_Book")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "bcit_user@bookrider.pl", roles = {"user"})
    void whenUserGetsTitlesWithInputFromMiddle_thenReturnOnlySelectedTitles() throws Exception {

        List<String> authors1 = List.of("Walter White", "Jessie Pinkman");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book1", "04032025121", 2015, "random.com");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book2", "04032025122", 2015, "random.com");
        createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book3", "04032025123", 2015, "random.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search-book-titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("title", "_Book")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "example_sys_admin@bookrider.pl", roles = {"system_administrator"})
    void whenSysAdminRemovesBook_thenReturnNoContent() throws Exception {

        List<String> authors1 = List.of("Walter White", "Jessie Pinkman");
        Book book = createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book1", "04032025121", 2015, "random.com");

        List<Book> books = libraryReference.getBooks();
        books.add(book);
        libraryReference.setBooks(books);
        libraryRepository.save(libraryReference);

        List<Library> libraries = book.getLibraries();
        libraries.add(libraryReference);
        book.setLibraries(libraries);
        bookRepository.save(book);

        libraryReference = libraryRepository.findById(1).orElseThrow();
        books = libraryReference.getBooks();
        assertTrue(books.contains(book));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        libraryReference = libraryRepository.findById(1).orElseThrow();
        books = libraryReference.getBooks();
        assertFalse(books.contains(book));
    }

    @Test
    @WithMockUser(username = "librarian67:1", roles = {"librarian"})
    void whenLibrarianRemovesBook_thenReturnNoContent() throws Exception {

        List<String> authors1 = List.of("Walter White", "Jessie Pinkman");
        Book book = createBook("Poetry", "Polish", "Oxford", authors1, "BCIT_Book1", "04032025121", 2015, "random.com");

        List<Book> books = libraryReference.getBooks();
        books.add(book);
        libraryReference.setBooks(books);
        libraryRepository.save(libraryReference);

        List<Library> libraries = book.getLibraries();
        libraries.add(libraryReference);
        book.setLibraries(libraries);
        bookRepository.save(book);

        libraryReference = libraryRepository.findById(1).orElseThrow();
        books = libraryReference.getBooks();
        assertTrue(books.contains(book));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/books/my-library/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        libraryReference = libraryRepository.findById(1).orElseThrow();
        books = libraryReference.getBooks();
        assertFalse(books.contains(book));
    }
}
