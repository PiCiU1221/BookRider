package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CreateAddressDTO;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class ShoppingCartControllerIT {

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
    private ObjectMapper objectMapper;

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
    private LibraryRepository libraryRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    private User userReference;

    @BeforeEach
    void setUp() {

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
        user.setEmail("testuser@sccit.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setDeliveryAddress(savedUserAddress);
        user.setShoppingCart(shoppingCart);

        userReference = userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "testuser@sccit.com", roles = {"user"})
    void whenValidAddressData_thenReturnOkAndSetAddress() throws Exception {

        CreateAddressDTO addressDTO = new CreateAddressDTO(
                "Władysława Jagiełły 12",
                "Szczecin",
                "70-262"
        );

        String jsonBody = objectMapper.writeValueAsString(addressDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/shopping-cart/address")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Address userAddress = userReference.getShoppingCart().getDeliveryAddress();
        assertEquals("Władysława Jagiełły 12", userAddress.getStreet());
    }

    @Test
    @WithMockUser(username = "testuser@sccit.com", roles = {"user"})
    void whenValidQuoteInput_thenReturnOkAndUpdateCart() throws Exception {

        Category category = new Category();
        category.setName("Category");
        Category savedCategory = categoryRepository.save(category);

        Language language = new Language();
        language.setName("Language");
        Language savedLanguage = languageRepository.save(language);

        Publisher publisher = new Publisher();
        publisher.setName("Publisher");
        Publisher savedPublisher = publisherRepository.save(publisher);

        Author author = new Author();
        author.setName("Author");
        Author savedAuthor = authorRepository.save(author);

        List<Author> authors = new ArrayList<>(){{
            add(savedAuthor);
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

        QuoteOption option = new QuoteOption();
        option.setLibrary(savedLibrary);
        option.setDistanceKm(BigDecimal.valueOf(10.0));
        option.setTotalDeliveryCost(BigDecimal.valueOf(10.50));
        option.setLibraryName(savedLibrary.getName());

        Quote quote = new Quote();
        quote.setValidUntil(LocalDateTime.now().plusMinutes(15));
        quote.setBook(savedBook);
        quote.setQuantity(1);
        option.setQuote(quote);
        List<QuoteOption> options = new ArrayList<>() {{
            add(option);
        }};
        quote.setOptions(options);

        Quote savedQuote = quoteRepository.save(quote);
        Integer quoteOptionId = savedQuote.getOptions().get(0).getId();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/shopping-cart/add-quote-option/{quoteOptionId}", quoteOptionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, userReference.getShoppingCart().getItems().size());
    }

    @Test
    @WithMockUser(username = "testuser@sccit.com", roles = {"user"})
    void whenValidInputData_thenReturnOkAndDeleteItemFromCart() throws Exception {

        Category category = new Category();
        category.setName("Category");
        Category savedCategory = categoryRepository.save(category);

        Language language = new Language();
        language.setName("Language");
        Language savedLanguage = languageRepository.save(language);

        Publisher publisher = new Publisher();
        publisher.setName("Publisher");
        Publisher savedPublisher = publisherRepository.save(publisher);

        Author author = new Author();
        author.setName("Author");
        Author savedAuthor = authorRepository.save(author);

        List<Author> authors = new ArrayList<>(){{
            add(savedAuthor);
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

        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setShoppingCart(userReference.getShoppingCart());
        shoppingCartItem.setLibrary(savedLibrary);
        shoppingCartItem.setTotalItemDeliveryCost(BigDecimal.valueOf(10.00));

        ShoppingCartSubItem shoppingCartSubItem = new ShoppingCartSubItem();
        shoppingCartSubItem.setShoppingCartItem(shoppingCartItem);
        shoppingCartSubItem.setBook(savedBook);
        shoppingCartSubItem.setQuantity(1);

        shoppingCartItem.getBooks().add(shoppingCartSubItem);

        ShoppingCart userCart = userReference.getShoppingCart();
        userCart.getItems().add(shoppingCartItem);

        shoppingCartRepository.save(userCart);

        Integer subItemId = userCart.getItems().get(0).getBooks().get(0).getId();

        assertEquals(1, userCart.getItems().size());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/shopping-cart/delete-sub-item/{subItemId}", subItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(0, userCart.getItems().size());
    }

    @Test
    @WithMockUser(username = "testuser@sccit.com", roles = {"user"})
    void whenValidUser_thenReturnOkAndUserCart() throws Exception {

        Category category = new Category();
        category.setName("Category");
        Category savedCategory = categoryRepository.save(category);

        Language language = new Language();
        language.setName("Language");
        Language savedLanguage = languageRepository.save(language);

        Publisher publisher = new Publisher();
        publisher.setName("Publisher");
        Publisher savedPublisher = publisherRepository.save(publisher);

        Author author = new Author();
        author.setName("Author");
        Author savedAuthor = authorRepository.save(author);

        List<Author> authors = new ArrayList<>(){{
            add(savedAuthor);
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

        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setShoppingCart(userReference.getShoppingCart());
        shoppingCartItem.setLibrary(savedLibrary);
        shoppingCartItem.setTotalItemDeliveryCost(BigDecimal.valueOf(10.00));

        ShoppingCartSubItem shoppingCartSubItem = new ShoppingCartSubItem();
        shoppingCartSubItem.setShoppingCartItem(shoppingCartItem);
        shoppingCartSubItem.setBook(savedBook);
        shoppingCartSubItem.setQuantity(1);

        shoppingCartItem.getBooks().add(shoppingCartSubItem);

        ShoppingCart userCart = userReference.getShoppingCart();
        userCart.getItems().add(shoppingCartItem);

        shoppingCartRepository.save(userCart);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/shopping-cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }
}
