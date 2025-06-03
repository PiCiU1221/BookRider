package edu.zut.bookrider.integration.controller;

import edu.zut.bookrider.model.*;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.PaymentStatus;
import edu.zut.bookrider.model.enums.RentalReturnStatus;
import edu.zut.bookrider.model.enums.RentalStatus;
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
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class RentalControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserIdGeneratorService userIdGeneratorService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private LanguageRepository languageRepository;
    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RentalReturnRepository rentalReturnRepository;

    private User user;
    private User driver;
    private Library library1;
    private Library library2;
    private Address address;

    private Address createAddress(String postalCode, String city, String street, double lat, double lon) {
        Address address = new Address();
        address.setPostalCode(postalCode);
        address.setCity(city);
        address.setStreet(street);
        address.setLatitude(BigDecimal.valueOf(lat));
        address.setLongitude(BigDecimal.valueOf(lon));
        return addressRepository.save(address);
    }

    private User createUser(String email, String roleName, double balance) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("password"));
        user.setBalance(BigDecimal.valueOf(balance));
        return userRepository.save(user);
    }

    private Library createLibrary(String name, String postalCode, String city, String street, double lat, double lon) {
        Address libraryAddress = createAddress(postalCode, city, street, lat, lon);
        Library library = new Library();
        library.setAddress(libraryAddress);
        library.setName(name);
        return libraryRepository.save(library);
    }

    private Author createAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        return authorRepository.save(author);
    }

    private Language createLanguage(String name) {
        Language language = new Language();
        language.setName(name);
        return languageRepository.save(language);
    }

    private Book createBook(String title, String isbn, Language language, Author author) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setLanguage(language);
        book.setAuthors(Collections.singletonList(author));
        return bookRepository.save(book);
    }

    private Order createOrder(User user, User driver, Library library, Address destination, Book book, int quantity, OrderStatus orderStatus, boolean isReturn) {
        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(destination);
        order.setIsReturn(isReturn);
        order.setStatus(orderStatus);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);

        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(quantity);
        orderItem.setBook(book);
        orderItem.setOrder(order);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);

        return orderRepository.save(order);
    }

    private Rental createRental(Order order, int quantity, RentalStatus status) {
        Rental rental = new Rental();
        rental.setUser(order.getUser());
        rental.setBook(order.getOrderItems().get(0).getBook());
        rental.setLibrary(order.getLibrary());
        rental.setOrder(order);
        rental.setQuantity(quantity);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(status);
        return rentalRepository.save(rental);
    }

    @BeforeEach
    void setUp() {
        address = createAddress("70-201", "Szczecin", "Wyszynskiego 10", 53.424451, 14.552580);
        user = createUser("testuser@rcit.com", "user", 9999);
        driver = createUser("testdriver@rcit.com", "driver", 0);

        library1 = createLibrary("Library1", "70-426", "Szczecin", "Generała Ludomiła Rayskiego 3", 53.434882, 14.552266);
        library2 = createLibrary("Library2", "70-426", "Szczecin", "Księdza Piotra Wawrzyniaka 13", 53.437720, 14.531702);
    }

    @Test
    @WithMockUser(username = "testuser@rcit.com", roles = {"user"})
    void whenUserRequestsRentals_thenReturnRentalsPage() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library1, address, book2, 1, OrderStatus.PENDING, false);

        createRental(order1, 2, RentalStatus.RENTED);
        createRental(order2, 1, RentalStatus.RENTED);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "testuser@rcit.com", roles = {"user"})
    void whenUserRequestsRentalsWithDifferentStatuses_thenReturnRentalsPage() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library1, address, book2, 1, OrderStatus.PENDING, false);

        createRental(order1, 2, RentalStatus.RENTED);
        createRental(order2, 1, RentalStatus.PARTIALLY_RETURNED);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "testuser@rcit.com", roles = {"user"})
    void whenUserRequestsRentalsWithPartialReturns_thenReturnRentalsPageWithCorrectQuantities() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library1, address, book2, 1, OrderStatus.PENDING, false);

        createRental(order1, 2, RentalStatus.RENTED);
        Rental rental1 = createRental(order2, 3, RentalStatus.PARTIALLY_RETURNED);

        Order returnOrder = createOrder(user, driver, library1, address, book2, 2, OrderStatus.AWAITING_LIBRARY_CONFIRMATION, true);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental1);
        rentalReturnItem.setBook(rental1.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturn = rentalReturnRepository.save(rentalReturn);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[1].quantity").value(1));
    }
}
