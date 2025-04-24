package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.dto.GeneralRentalReturnRequestDTO;
import edu.zut.bookrider.dto.InPersonRentalReturnRequestDTO;
import edu.zut.bookrider.dto.RentalReturnWithQuantityRequestDTO;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class RentalReturnControllerIT {

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
    private Library existingLibrary;
    private User librarian;
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
        user.setBalance(BigDecimal.valueOf(balance).setScale(2, RoundingMode.CEILING));
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

    private Rental createRental(Order order, int quantity, boolean addDays) {
        Rental rental = new Rental();
        rental.setUser(order.getUser());
        rental.setBook(order.getOrderItems().get(0).getBook());
        rental.setLibrary(order.getLibrary());
        rental.setOrder(order);
        rental.setQuantity(quantity);

        if (addDays) {
            rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        } else {
            rental.setReturnDeadline(LocalDateTime.now().minusDays(30));
        }

        rental.setStatus(RentalStatus.RENTED);
        return rentalRepository.save(rental);
    }

    @BeforeEach
    void setUp() {
        user = createUser("testuser@rrcit.com", "user", 9999);
        driver = createUser("testdriver@rrcit.com", "driver", 0);

        existingLibrary = libraryRepository.findById(1).orElseThrow();

        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setRole(librarianRole);
        librarian.setUsername("librarianRrcit");
        librarian.setFirstName("Adam");
        librarian.setLastName("Smith");
        librarian.setLibrary(existingLibrary);
        librarian.setPassword(passwordEncoder.encode("password"));
        librarian = userRepository.save(librarian);

        library1 = createLibrary("Library1", "70-426", "Szczecin", "Generała Ludomiła Rayskiego 3", 53.434882, 14.552266);
        library2 = createLibrary("Library2", "70-426", "Szczecin", "Księdza Piotra Wawrzyniaka 13", 53.437720, 14.531702);

        // Address has to be real and if it existed in the database before the test it would throw an exception.
        Optional<Address> existingAddress = addressRepository.findByStreetAndCityAndPostalCode(
                "Wyszynskiego 10", "Szczecin", "70-201"
        );

        if (existingAddress.isPresent()) {
            address = existingAddress.get();
        } else {
            address = createAddress("70-201", "Szczecin", "Wyszynskiego 10", 53.424451, 14.552580);
        }
    }

    private GeneralRentalReturnRequestDTO prepareReturnRequest(Address address, List<Rental> rentals) {
        CreateAddressDTO createAddressDTO = new CreateAddressDTO();
        createAddressDTO.setStreet(address.getStreet());
        createAddressDTO.setCity(address.getCity());
        createAddressDTO.setPostalCode(address.getPostalCode());

        List<RentalReturnWithQuantityRequestDTO> rentalReturnRequests = rentals.stream()
                .map(rental -> {
                    RentalReturnWithQuantityRequestDTO requestDTO = new RentalReturnWithQuantityRequestDTO();
                    requestDTO.setRentalId(rental.getId());
                    requestDTO.setQuantityToReturn(1);
                    return requestDTO;
                })
                .collect(Collectors.toList());

        GeneralRentalReturnRequestDTO request = new GeneralRentalReturnRequestDTO();
        request.setCreateAddressDTO(createAddressDTO);
        request.setRentalReturnRequests(rentalReturnRequests);
        return request;
    }

    private InPersonRentalReturnRequestDTO prepareInPersonReturnRequest(List<Rental> rentals) {

        List<RentalReturnWithQuantityRequestDTO> rentalReturnRequests = rentals.stream()
                .map(rental -> {
                    RentalReturnWithQuantityRequestDTO requestDTO = new RentalReturnWithQuantityRequestDTO();
                    requestDTO.setRentalId(rental.getId());
                    requestDTO.setQuantityToReturn(1);
                    return requestDTO;
                })
                .collect(Collectors.toList());

        InPersonRentalReturnRequestDTO request = new InPersonRentalReturnRequestDTO();
        request.setRentalReturnRequests(rentalReturnRequests);
        return request;
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserOrdersTwoReturnsFromTwoLibraries_thenCreateTwoReturnOrders() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, true);
        Rental rental2 = createRental(order2, 1, true);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = prepareReturnRequest(address, rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        assertEquals(BigDecimal.valueOf(9976.30).setScale(2, RoundingMode.CEILING), user.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserOrdersTwoReturnsFromTwoLibrariesWithLateFees_thenCreateTwoReturnOrders() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, false);
        Rental rental2 = createRental(order2, 1, false);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = prepareReturnRequest(address, rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        assertEquals(BigDecimal.valueOf(9916.30).setScale(2, RoundingMode.CEILING), user.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserOrdersTwoReturnsFromOneLibrary_thenCreateOneReturnOrder() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library1, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, true);
        Rental rental2 = createRental(order2, 1, true);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = prepareReturnRequest(address, rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rentalReturnItems.length()").value(2));

        assertEquals(BigDecimal.valueOf(9986.90).setScale(2, RoundingMode.CEILING), user.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserCalculatesTwoReturnOrdersFromTwoLibrariesWithoutFees_thenReturnPriceAndDontCreateAnything() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, true);
        Rental rental2 = createRental(order2, 1, true);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = prepareReturnRequest(address, rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/calculate-price")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(22.70))
                .andExpect(jsonPath("$.deliveryCost").value(22.70))
                .andExpect(jsonPath("$.totalLateFees").value(0.00))
                .andExpect(jsonPath("$.lateFees.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserCalculatesTwoReturnOrdersFromTwoLibrariesWithFees_thenReturnPriceAndDontCreateAnything() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, false);
        Rental rental2 = createRental(order2, 1, false);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = prepareReturnRequest(address, rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/calculate-price")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(82.70))
                .andExpect(jsonPath("$.deliveryCost").value(22.70))
                .andExpect(jsonPath("$.totalLateFees").value(60.0))
                .andExpect(jsonPath("$.lateFees.length()").value(2));
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenDtoRentalReturnListEmpty_thenReturnBadRequest() throws Exception {

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = new GeneralRentalReturnRequestDTO();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/calculate-price")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "librarianRrcit:1", roles = {"librarian"})
    void whenLibrarianRequestsLatestDriversReturn_thenReturnOkAndLatestReturn() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, existingLibrary, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        Order returnOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.AWAITING_LIBRARY_CONFIRMATION, true);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturnRepository.save(rentalReturn);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rental-returns/latest-by-driver/{driverId}", driver.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "librarianRrcit:1", roles = {"librarian"})
    void whenLibrarianRequestsLatestDriversReturnWhenTheyDontExist_thenReturnNotFound() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rental-returns/latest-by-driver/{driverId}", driver.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "librarianRrcit:1", roles = {"librarian"})
    void whenLibrarianRequestsDeliveryReturnCompletion_thenReturnNoContent() throws Exception {

        Library library1 = createLibrary("Library1", "70-426", "Szczecin", "Generała Ludomiła Rayskiego 3", 53.434882, 14.552266);

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        Order returnOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.AWAITING_LIBRARY_CONFIRMATION, true);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturn = rentalReturnRepository.save(rentalReturn);
        assertEquals(RentalReturnStatus.IN_PROGRESS, rentalReturn.getStatus());

        assertEquals(BigDecimal.valueOf(0.00).setScale(2, RoundingMode.CEILING), driver.getBalance());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/rental-returns/{rentalReturnId}/complete-delivery", rentalReturn.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        rentalReturn = rentalReturnRepository.findById(rentalReturn.getId()).orElseThrow();
        assertEquals(RentalReturnStatus.COMPLETED, rentalReturn.getStatus());

        assertEquals(BigDecimal.valueOf(8.00).setScale(2, RoundingMode.CEILING), driver.getBalance());
    }

    @Test
    @WithMockUser(username = "librarianRrcit:1", roles = {"librarian"})
    void whenLibrarianRequestsInPersonReturnCompletion_thenReturnNoContent() throws Exception {

        Library library1 = createLibrary("Library1", "70-426", "Szczecin", "Generała Ludomiła Rayskiego 3", 53.434882, 14.552266);

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setStatus(RentalReturnStatus.IN_PERSON);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturn = rentalReturnRepository.save(rentalReturn);
        assertEquals(RentalReturnStatus.IN_PERSON, rentalReturn.getStatus());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/rental-returns/{rentalReturnId}/complete-in-person", rentalReturn.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        rentalReturn = rentalReturnRepository.findById(rentalReturn.getId()).orElseThrow();
        assertEquals(RentalReturnStatus.COMPLETED, rentalReturn.getStatus());
    }

    @Test
    @WithMockUser(username = "librarianRrcit:1", roles = {"librarian"})
    void whenLibrarianRequestsInPersonReturnCompletionForDeliveryReturn_thenReturnNoContent() throws Exception {

        Library library1 = createLibrary("Library1", "70-426", "Szczecin", "Generała Ludomiła Rayskiego 3", 53.434882, 14.552266);

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturnRepository.save(rentalReturn);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/rental-returns/{rentalReturnId}/complete-in-person", rentalReturn.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "librarianRrcit:1", roles = {"librarian"})
    void whenLibrarianRequestsInPersonReturnDTOById_thenReturnOkAndRentalReturnDTO() throws Exception {

        Library library1 = createLibrary("Library1", "70-426", "Szczecin", "Generała Ludomiła Rayskiego 3", 53.434882, 14.552266);

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setStatus(RentalReturnStatus.IN_PERSON);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturn = rentalReturnRepository.save(rentalReturn);
        assertEquals(RentalReturnStatus.IN_PERSON, rentalReturn.getStatus());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rental-returns/{rentalReturnId}", rentalReturn.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalReturnItems.length()").value(1));
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserOrdersTwoReturnsFromOneLibraryInPersonWithoutFees_thenCreateOneInPersonReturn() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library1, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, true);
        Rental rental2 = createRental(order2, 1, true);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        InPersonRentalReturnRequestDTO inPersonRentalReturnRequestDTO = prepareInPersonReturnRequest(rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/in-person")
                        .content(new ObjectMapper().writeValueAsString(inPersonRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rentalReturnItems.length()").value(2));

        assertEquals(BigDecimal.valueOf(9999.00).setScale(2, RoundingMode.CEILING), user.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserOrdersTwoReturnsFromTwoLibrariesInPersonWithoutFees_thenCreateTwoInPersonReturns() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, true);
        Rental rental2 = createRental(order2, 1, true);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        InPersonRentalReturnRequestDTO inPersonRentalReturnRequestDTO = prepareInPersonReturnRequest(rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/in-person")
                        .content(new ObjectMapper().writeValueAsString(inPersonRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        assertEquals(BigDecimal.valueOf(9999.00).setScale(2, RoundingMode.CEILING), user.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserOrdersTwoReturnsFromTwoLibrariesInPersonWithLateFees_thenCreateTwoInPersonReturns() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, false);
        Rental rental2 = createRental(order2, 1, false);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        InPersonRentalReturnRequestDTO inPersonRentalReturnRequestDTO = prepareInPersonReturnRequest(rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/in-person")
                        .content(new ObjectMapper().writeValueAsString(inPersonRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        assertEquals(BigDecimal.valueOf(9939.00).setScale(2, RoundingMode.CEILING), user.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserOrdersTwoReturnsFromTwoLibrariesInPersonWithLateFeesWhenNotEnoughBalance_thenReturnBadRequest() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, false);
        Rental rental2 = createRental(order2, 1, false);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        InPersonRentalReturnRequestDTO inPersonRentalReturnRequestDTO = prepareInPersonReturnRequest(rentalList);

        user.setBalance(BigDecimal.ZERO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/in-person")
                        .content(new ObjectMapper().writeValueAsString(inPersonRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserCalculatesTwoInPersonReturnsFromTwoLibrariesWithoutLateFees_thenReturnPriceAndDontCreateAnything() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, true);
        Rental rental2 = createRental(order2, 1, true);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = prepareReturnRequest(address, rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/in-person/calculate-price")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(0.00))
                .andExpect(jsonPath("$.deliveryCost").value(0.00))
                .andExpect(jsonPath("$.totalLateFees").value(0.00))
                .andExpect(jsonPath("$.lateFees.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserCalculatesTwoInPersonReturnsFromTwoLibrariesWithLateFees_thenReturnPriceAndDontCreateAnything() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book1 = createBook("Book1", "113109022025", language, author);
        Book book2 = createBook("Book2", "113209022025", language, author);

        Order order1 = createOrder(user, driver, library1, address, book1, 2, OrderStatus.PENDING, false);
        Order order2 = createOrder(user, driver, library2, address, book2, 1, OrderStatus.PENDING, false);

        Rental rental1 = createRental(order1, 2, false);
        Rental rental2 = createRental(order2, 1, false);

        List<Rental> rentalList = Arrays.asList(rental1, rental2);

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = prepareReturnRequest(address, rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns/in-person/calculate-price")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(60.00))
                .andExpect(jsonPath("$.deliveryCost").value(0.00))
                .andExpect(jsonPath("$.totalLateFees").value(60.00))
                .andExpect(jsonPath("$.lateFees.length()").value(2));
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserHandsOverReturnOrder_thenChangeOrderStatusToInTransit() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        Order returnOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DRIVER_ACCEPTED, true);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturn = rentalReturnRepository.save(rentalReturn);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rental-returns/" + rentalReturn.getId() + "/handover")
                        .param("driverId", driver.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        rentalReturn = rentalReturnRepository.findById(rentalReturn.getId()).orElseThrow();
        assertEquals(OrderStatus.IN_TRANSIT, rentalReturn.getReturnOrder().getStatus());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserHandsOverReturnOrderToWrongDriver_thenReturnBadRequest() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        Order returnOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DRIVER_ACCEPTED, true);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturn = rentalReturnRepository.save(rentalReturn);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rental-returns/" + rentalReturn.getId() + "/handover")
                        .param("driverId", "something")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserRequestsRentalReturnOrders_thenReturnRentalReturnDTOPage() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        Order returnOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DRIVER_ACCEPTED, true);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturnRepository.save(rentalReturn);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rental-returns")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rentalReturnItems.length()").value(1));
    }

    @Test
    @WithMockUser(username = "testuser@rrcit.com:user", roles = {"user"})
    void whenUserRequestsReturnOrderForAlreadyReturnedReturn_thenReturnConflict() throws Exception {

        Author author = createAuthor("Author");
        Language language = createLanguage("language");

        Book book = createBook("Book1", "113109022025", language, author);

        Order deliveryOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DELIVERED, false);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBook(deliveryOrder.getOrderItems().get(0).getBook());
        rental.setLibrary(deliveryOrder.getLibrary());
        rental.setOrder(deliveryOrder);
        rental.setQuantity(2);
        rental.setReturnDeadline(LocalDateTime.now().plusDays(30));
        rental.setStatus(RentalStatus.RENTED);
        rental = rentalRepository.save(rental);

        Order returnOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.DRIVER_ACCEPTED, true);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setBook(rental.getBook());
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturnRepository.save(rentalReturn);

        List<Rental> rentalList = List.of(rental);

        GeneralRentalReturnRequestDTO generalRentalReturnRequestDTO = prepareReturnRequest(address, rentalList);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rental-returns")
                        .content(new ObjectMapper().writeValueAsString(generalRentalReturnRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
