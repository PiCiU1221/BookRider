package edu.zut.bookrider.integration.controller;

import edu.zut.bookrider.model.*;
import edu.zut.bookrider.repository.*;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class QuoteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserIdGeneratorService userIdGeneratorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Integer bookId;
    private Integer quantity;

    @BeforeEach
    void setUp() {

        quantity = 2;

        Category category = new Category();
        category.setName("Category");
        Category savedCategory = categoryRepository.save(category);

        Language language = new Language();
        language.setName("Language");
        Language savedLanguage = languageRepository.save(language);

        Publisher publisher = new Publisher();
        publisher.setName("Publisher");
        Publisher savedPublisher = publisherRepository.save(publisher);

        Author author1 = new Author();
        author1.setName("Author1");
        Author savedAuthor1 = authorRepository.save(author1);

        Author author2 = new Author();
        author2.setName("Author2");
        Author savedAuthor2 = authorRepository.save(author2);

        List<Author> authors = new ArrayList<>(){{
            add(savedAuthor1);
            add(savedAuthor2);
        }};

        Book book = new Book();
        book.setCategory(savedCategory);
        book.setLanguage(savedLanguage);
        book.setPublisher(savedPublisher);
        book.setTitle("Book title");
        book.setIsbn("12312312312");
        book.setReleaseYear(1234);
        book.setAuthors(authors);
        Book savedBook = bookRepository.save(book);

        Address libraryAddress = new Address();
        libraryAddress.setStreet("Something");
        libraryAddress.setCity("Something");
        libraryAddress.setPostalCode("something");
        libraryAddress.setLatitude(BigDecimal.valueOf(53.41));
        libraryAddress.setLongitude(BigDecimal.valueOf(14.49));
        Address savedLibraryAddress = addressRepository.save(libraryAddress);

        List<Book> books = new ArrayList<>(){{
            add(savedBook);
        }};

        Library library = new Library();
        library.setAddress(savedLibraryAddress);
        library.setName("testLibrary");
        library.setPhoneNumber("123123123");
        library.setEmail("library_email@test.com");
        library.setBooks(books);
        Library savedLibrary = libraryRepository.save(library);

        bookId = savedLibrary.getBooks().get(0).getId();

        Address userDeliveryAddress = new Address();
        userDeliveryAddress.setStreet("Something");
        userDeliveryAddress.setCity("Something");
        userDeliveryAddress.setPostalCode("something");
        userDeliveryAddress.setLatitude(BigDecimal.valueOf(53.432));
        userDeliveryAddress.setLongitude(BigDecimal.valueOf(14.55));
        Address savedUserAddress = addressRepository.save(userDeliveryAddress);

        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("testuser@qcit.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setDeliveryAddress(savedUserAddress);
        user.setShoppingCart(shoppingCart);

        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "testuser@qcit.com", roles = {"user"})
    void whenValidData_thenReturnOk() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/quotes")
                        .param("bookId", bookId.toString())
                        .param("quantity", quantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser@qcit.com", roles = {"user"})
    void whenInvalidQuantity_thenReturnBadRequest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/quotes")
                        .param("bookId", bookId.toString())
                        .param("quantity", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@qcit.com", roles = {"user"})
    void whenBookDoesntExist_thenReturnBadRequest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/quotes")
                        .param("bookId", "0")
                        .param("quantity", quantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
