package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CreateOrderResponseDTO;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class CheckoutControllerIT {

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
        user.setEmail("testuser@ccit.com");
        user.setRole(userRole);
        user.setBalance(BigDecimal.valueOf(9999));
        user.setPassword(passwordEncoder.encode("password"));

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setDeliveryAddress(savedUserAddress);
        user.setShoppingCart(shoppingCart);

        userReference = userRepository.save(user);

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
    }

    @Test
    @WithMockUser(username = "testuser@ccit.com", roles = {"user"})
    void whenShoppingCartContainsOneItem_thenCreateOneOrder() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<CreateOrderResponseDTO> orders = objectMapper.readValue(
                responseContent,
                new TypeReference<>() {
                }
        );

        assertEquals(1, orders.size(), "The returned orders list should contain only one item");

        CreateOrderResponseDTO order = orders.get(0);
        assertNotNull(order.getOrderItems(), "Order items list should not be null");
        assertEquals(1, order.getOrderItems().size(), "The orderItems list should contain only one object");

        assertEquals(0, userReference.getShoppingCart().getItems().size(), "Shopping cart should be empty after checkout");
    }

    @Test
    @WithMockUser(username = "testuser@ccit.com", roles = {"user"})
    void whenShoppingCartContainsTwoItems_thenCreateTwoOrders() throws Exception {

        Category category = new Category();
        category.setName("Category2");
        Category savedCategory = categoryRepository.save(category);

        Language language = new Language();
        language.setName("Language2");
        Language savedLanguage = languageRepository.save(language);

        Publisher publisher = new Publisher();
        publisher.setName("Publisher2");
        Publisher savedPublisher = publisherRepository.save(publisher);

        Author author = new Author();
        author.setName("Author2");
        Author savedAuthor = authorRepository.save(author);

        List<Author> authors = new ArrayList<>(){{
            add(savedAuthor);
        }};

        Book book = new Book();
        book.setCategory(savedCategory);
        book.setLanguage(savedLanguage);
        book.setPublisher(savedPublisher);
        book.setTitle("Book title2");
        book.setIsbn("12312312311");
        book.setReleaseYear(1234);
        book.setAuthors(authors);
        Book savedBook = bookRepository.save(book);

        Address libraryAddress = new Address();
        libraryAddress.setStreet("Something2");
        libraryAddress.setCity("Something2");
        libraryAddress.setPostalCode("Something2");
        libraryAddress.setLatitude(BigDecimal.valueOf(53.40));
        libraryAddress.setLongitude(BigDecimal.valueOf(14.49));
        Address savedLibraryAddress = addressRepository.save(libraryAddress);

        List<Book> books = new ArrayList<>(){{
            add(savedBook);
        }};

        Library library = new Library();
        library.setAddress(savedLibraryAddress);
        library.setName("testLibrary2");
        library.setPhoneNumber("123123121");
        library.setEmail("library2_email@test.com");
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

        Book book2 = new Book();
        book2.setCategory(savedCategory);
        book2.setLanguage(savedLanguage);
        book2.setPublisher(savedPublisher);
        book2.setTitle("Book title2");
        book2.setIsbn("12312312315");
        book2.setReleaseYear(1234);
        book2.setAuthors(authors);
        Book savedBook2 = bookRepository.save(book2);

        ShoppingCartSubItem shoppingCartSubItem2 = new ShoppingCartSubItem();
        shoppingCartSubItem2.setShoppingCartItem(shoppingCartItem);
        shoppingCartSubItem2.setBook(savedBook2);
        shoppingCartSubItem2.setQuantity(1);

        shoppingCartItem.getBooks().add(shoppingCartSubItem);
        shoppingCartItem.getBooks().add(shoppingCartSubItem2);

        ShoppingCart userCart = userReference.getShoppingCart();
        userCart.getItems().add(shoppingCartItem);

        shoppingCartRepository.save(userCart);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<CreateOrderResponseDTO> orders = objectMapper.readValue(
                responseContent,
                new TypeReference<>() {
                }
        );

        assertEquals(2, orders.size(), "The returned orders list should contain two items");

        CreateOrderResponseDTO order1 = orders.get(0);
        assertNotNull(order1.getOrderItems(), "Order items list should not be null");
        assertEquals(1, order1.getOrderItems().size(), "The orderItems list should contain only one object");

        CreateOrderResponseDTO order2 = orders.get(1);
        assertNotNull(order2.getOrderItems(), "Order items list should not be null");
        assertEquals(2, order2.getOrderItems().size(), "The orderItems list should contain two objects");

        assertEquals(0, userReference.getShoppingCart().getItems().size(), "Shopping cart should be empty after checkout");
    }

    @Test
    @WithMockUser(username = "testuser@ccit.com", roles = {"user"})
    void whenShoppingCartEmpty_thenReturnBadRequest() throws Exception {

        ShoppingCart userCart = userReference.getShoppingCart();
        userCart.getItems().clear();
        shoppingCartRepository.save(userCart);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@ccit.com", roles = {"user"})
    void whenNotEnoughBalance_thenReturnBadRequest() throws Exception {

        userReference.setBalance(BigDecimal.valueOf(0));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
