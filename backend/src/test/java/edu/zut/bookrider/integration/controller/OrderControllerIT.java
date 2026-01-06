package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.DeliverOrderRequestDTO;
import edu.zut.bookrider.dto.DeliveryNavigationRequestDTO;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.PaymentStatus;
import edu.zut.bookrider.model.enums.RentalReturnStatus;
import edu.zut.bookrider.model.enums.RentalStatus;
import edu.zut.bookrider.repository.*;
import edu.zut.bookrider.service.UserIdGeneratorService;
import jakarta.transaction.Transactional;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    This test uses a real instance of the first library (libraryId = 1).
    Can't use @WithMockUser with variables, and our system requires
    library ID in the username for this annotation. This is just a
    temporary solution until we will use some more sophisticated methods
    to do it. For development purposes, it's fine for now.
 */
@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserIdGeneratorService userIdGeneratorService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private LanguageRepository languageRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private RentalReturnRepository rentalReturnRepository;

    private User user;
    private User librarian;
    private User driver;
    private Library library;
    private Address address;

    @BeforeEach
    void setUp() {

        address = new Address();
        address.setPostalCode("12312");
        address.setCity("Szczecin");
        address.setStreet("Wyszynskiego 10");
        address.setLatitude(BigDecimal.valueOf(53.424451));
        address.setLongitude(BigDecimal.valueOf(14.552580));
        address = addressRepository.save(address);

        Role userRole = roleRepository.findByName("user").orElseThrow();
        user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("testuser@ocit.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        user = userRepository.save(user);

        library = libraryRepository.findById(1).orElseThrow();

        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setUsername("testlibrarian1");
        librarian.setFirstName("Adam");
        librarian.setLastName("Smith");
        librarian.setRole(librarianRole);
        librarian.setLibrary(library);
        librarian.setPassword(passwordEncoder.encode("password"));
        librarian = userRepository.save(librarian);

        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        driver = new User();
        driver.setId(userIdGeneratorService.generateUniqueId());
        driver.setEmail("testdriver@ocit.com");
        driver.setRole(driverRole);
        driver.setPassword(passwordEncoder.encode("password"));
        driver = userRepository.save(driver);
    }

    @Test
    @WithMockUser(username = "testuser@ocit.com:user", roles = {"user"})
    void whenUserRequestsPendingOrders_thenReturnUserPendingOrders() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.PENDING);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/user/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @WithMockUser(username = "testuser@ocit.com:user", roles = {"user"})
    void whenUserRequestsInRealizationOrders_thenReturnUserInRealizationOrders() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUser(user);
        order3.setLibrary(library);
        order3.setPickupAddress(library.getAddress());
        order3.setDestinationAddress(address);
        order3.setIsReturn(false);
        order3.setStatus(OrderStatus.IN_TRANSIT);
        order3.setAmount(BigDecimal.valueOf(10));
        order3.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order3);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/user/in-realization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @WithMockUser(username = "testuser@ocit.com:user", roles = {"user"})
    void whenUserRequestsCompletedOrders_thenReturnUserCompletedOrders() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DELIVERED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.DECLINED);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/user/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianRequestsPendingOrders_thenReturnLibraryPendingOrders() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.PENDING);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUser(user);
        order3.setLibrary(library);
        order3.setPickupAddress(library.getAddress());
        order3.setDestinationAddress(address);
        order3.setIsReturn(false);
        order3.setStatus(OrderStatus.ACCEPTED);
        order3.setAmount(BigDecimal.valueOf(10));
        order3.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order3);

        Order order4 = new Order();
        order4.setUser(user);
        order4.setLibrary(library);
        order4.setPickupAddress(library.getAddress());
        order4.setDestinationAddress(address);
        order4.setIsReturn(false);
        order4.setStatus(OrderStatus.IN_TRANSIT);
        order4.setAmount(BigDecimal.valueOf(10));
        order4.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order4);

        Order order5 = new Order();
        order5.setUser(user);
        order5.setLibrary(library);
        order5.setPickupAddress(library.getAddress());
        order5.setDestinationAddress(address);
        order5.setIsReturn(false);
        order5.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order5.setAmount(BigDecimal.valueOf(10));
        order5.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order5);

        Order order6 = new Order();
        order6.setUser(user);
        order6.setLibrary(library);
        order6.setPickupAddress(library.getAddress());
        order6.setDestinationAddress(address);
        order6.setIsReturn(false);
        order6.setStatus(OrderStatus.DELIVERED);
        order6.setAmount(BigDecimal.valueOf(10));
        order6.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order6);

        Order order7 = new Order();
        order7.setUser(user);
        order7.setLibrary(library);
        order7.setPickupAddress(library.getAddress());
        order7.setDestinationAddress(address);
        order7.setIsReturn(false);
        order7.setStatus(OrderStatus.DECLINED);
        order7.setAmount(BigDecimal.valueOf(10));
        order7.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order7);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/librarian/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(2-1))))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", greaterThan(0)));
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianRequestsInRealizationOrders_thenReturnLibraryInRealizationOrders() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.ACCEPTED);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUser(user);
        order3.setLibrary(library);
        order3.setPickupAddress(library.getAddress());
        order3.setDestinationAddress(address);
        order3.setIsReturn(false);
        order3.setStatus(OrderStatus.PENDING);
        order3.setAmount(BigDecimal.valueOf(10));
        order3.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order3);

        Order order4 = new Order();
        order4.setUser(user);
        order4.setLibrary(library);
        order4.setPickupAddress(library.getAddress());
        order4.setDestinationAddress(address);
        order4.setIsReturn(false);
        order4.setStatus(OrderStatus.IN_TRANSIT);
        order4.setAmount(BigDecimal.valueOf(10));
        order4.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order4);

        Order order5 = new Order();
        order5.setUser(user);
        order5.setLibrary(library);
        order5.setPickupAddress(library.getAddress());
        order5.setDestinationAddress(address);
        order5.setIsReturn(false);
        order5.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order5.setAmount(BigDecimal.valueOf(10));
        order5.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order5);

        Order order6 = new Order();
        order6.setUser(user);
        order6.setLibrary(library);
        order6.setPickupAddress(library.getAddress());
        order6.setDestinationAddress(address);
        order6.setIsReturn(false);
        order6.setStatus(OrderStatus.DELIVERED);
        order6.setAmount(BigDecimal.valueOf(10));
        order6.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order6);

        Order order7 = new Order();
        order7.setUser(user);
        order7.setLibrary(library);
        order7.setPickupAddress(library.getAddress());
        order7.setDestinationAddress(address);
        order7.setIsReturn(false);
        order7.setStatus(OrderStatus.DECLINED);
        order7.setAmount(BigDecimal.valueOf(10));
        order7.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order7);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/librarian/in-realization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(2-1))))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", greaterThan(0)));
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianRequestsCompletedOrders_thenReturnLibraryCompletedOrders() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.DECLINED);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUser(user);
        order3.setLibrary(library);
        order3.setPickupAddress(library.getAddress());
        order3.setDestinationAddress(address);
        order3.setIsReturn(false);
        order3.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order3.setAmount(BigDecimal.valueOf(10));
        order3.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order3);

        Order order4 = new Order();
        order4.setUser(user);
        order4.setLibrary(library);
        order4.setPickupAddress(library.getAddress());
        order4.setDestinationAddress(address);
        order4.setIsReturn(false);
        order4.setStatus(OrderStatus.IN_TRANSIT);
        order4.setAmount(BigDecimal.valueOf(10));
        order4.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order4);

        Order order5 = new Order();
        order5.setUser(user);
        order5.setLibrary(library);
        order5.setPickupAddress(library.getAddress());
        order5.setDestinationAddress(address);
        order5.setIsReturn(false);
        order5.setStatus(OrderStatus.DELIVERED);
        order5.setAmount(BigDecimal.valueOf(10));
        order5.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order5);

        Order order6 = new Order();
        order6.setUser(user);
        order6.setLibrary(library);
        order6.setPickupAddress(library.getAddress());
        order6.setDestinationAddress(address);
        order6.setIsReturn(false);
        order6.setStatus(OrderStatus.DELIVERED);
        order6.setAmount(BigDecimal.valueOf(10));
        order6.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order6);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/librarian/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(4-1))))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", greaterThan(0)));
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianAcceptsOrder_thenStatusIsAccepted() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/orders/" + order.getId() + "/accept"))
                .andExpect(status().isOk());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.ACCEPTED, updatedOrder.getStatus());
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenOrderIsAlreadyAcceptedOrDeclined_thenIllegalStateExceptionIsThrown() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setLibrarian(librarian);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/orders/" + order.getId() + "/accept"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenOrderNotFound_thenOrderNotFoundExceptionIsThrown() throws Exception {

        int nonExistentOrderId = 999999999;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/orders/" + nonExistentOrderId + "/accept"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianDeclinesOrder_thenStatusIsDeclined() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        String rejectionReason = "The requested item is out of stock.";
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/orders/" + order.getId() + "/decline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"" + rejectionReason + "\"}"))
                .andExpect(status().isOk());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DECLINED, updatedOrder.getStatus());
        assertEquals(rejectionReason, updatedOrder.getDeclineReason());
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianHandoverOrderToDriver_thenStatusIsUpdated() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDriver(driver);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/" + order.getId() + "/handover")
                        .param("driverId", driver.getId()))
                .andExpect(status().isOk());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.IN_TRANSIT, updatedOrder.getStatus());
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenOrderIsNotAccepted_thenIllegalStateExceptionIsThrown() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDriver(driver);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/" + order.getId() + "/handover")
                        .param("driverId", driver.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenProvidedDriverIdDoesNotMatchAssignedDriver_thenIllegalArgumentExceptionIsThrown() throws Exception {

        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        User driver1 = new User();
        driver1.setId(userIdGeneratorService.generateUniqueId());
        driver1.setRole(driverRole);
        driver1.setEmail("testdriver1@ocit.com");
        driver1.setFirstName("Adam");
        driver1.setLastName("Driver");
        driver1.setPassword(passwordEncoder.encode("password"));
        driver1 = userRepository.save(driver1);

        User driver2 = new User();
        driver2.setId(userIdGeneratorService.generateUniqueId());
        driver2.setRole(driverRole);
        driver2.setEmail("testdriver2@ocit.com");
        driver2.setFirstName("Bob");
        driver2.setLastName("Driver");
        driver2.setPassword(passwordEncoder.encode("password"));
        driver2 = userRepository.save(driver2);

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDriver(driver1);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/" + order.getId() + "/handover")
                        .param("driverId", driver2.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverRequestsPendingOrders_thenReturnDriverPendingOrders() throws Exception {

        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setDriver(driver);
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.ACCEPTED);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/driver/pending")
                        .param("locationString", "53.425383,14.543055")
                        // since we're using real library this is very big to pass tests
                        .param("maxDistanceInMeters", "99999999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(2-1))))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", greaterThan(0)));
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverRequestsInRealizationOrders_thenReturnDriverInRealizationOrders() throws Exception {

        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setDriver(driver);
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.IN_TRANSIT);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setDriver(driver);
        order3.setUser(user);
        order3.setLibrary(library);
        order3.setPickupAddress(library.getAddress());
        order3.setDestinationAddress(address);
        order3.setIsReturn(true);
        order3.setStatus(OrderStatus.AWAITING_LIBRARY_CONFIRMATION);
        order3.setAmount(BigDecimal.valueOf(10));
        order3.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order3);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/driver/in-realization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverCompletedRealizationOrders_thenReturnDriverCompletedOrders() throws Exception {

        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DELIVERED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setDriver(driver);
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setPickupAddress(library.getAddress());
        order2.setDestinationAddress(address);
        order2.setIsReturn(false);
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/driver/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverAssignsOrder_thenReturnOkAndAssignIt() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        assertNull(order.getDriverAssignedAt());
        assertNull(order.getDriver());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/" + order.getId() + "/assign"))
                .andExpect(status().isOk());

        Order orderAfterRequest = orderRepository.findById(order.getId()).orElseThrow();
        assertNotNull(orderAfterRequest.getDriverAssignedAt());
        assertEquals(driver, orderAfterRequest.getDriver());
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverAssignsOrderWithDifferentStatus_thenThrowException() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        assertNull(order.getDriverAssignedAt());
        assertNull(order.getDriver());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/" + order.getId() + "/assign"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverDeliversOrder_thenReturnOkAndDriverPayoutTransactionDTO() throws Exception {

        Book newBook = new Book();
        List<Author> authors = new ArrayList<>();

        Author author = new Author();
        author.setName("Author");
        author = authorRepository.save(author);
        authors.add(author);
        newBook.setAuthors(authors);

        Language language = new Language();
        language.setName("language");
        language = languageRepository.save(language);
        newBook.setLanguage(language);

        newBook.setTitle("Title");
        newBook.setIsbn("07022025");
        Book savedBook = bookRepository.save(newBook);

        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.IN_TRANSIT);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);

        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(2);
        orderItem.setBook(savedBook);
        orderItem.setOrder(order);
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        order.setOrderItems(items);
        order = orderRepository.save(order);

        CoordinateDTO location = new CoordinateDTO(53.423, 14.553);

        String imagePath = "src/test/resources/orderControllerTest/example_delivery_photo.png";
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        DeliverOrderRequestDTO requestDTO = new DeliverOrderRequestDTO(
                location,
                base64Image
        );

        Optional<Rental> rentalOpt = rentalRepository.findByOrderIdAndBookId(order.getId(), savedBook.getId());
        assertFalse(rentalOpt.isPresent(), "Rental should not exist");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/deliver")
                        .content(new ObjectMapper().writeValueAsString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isPayoutProcessed").value(true))
                .andExpect(status().isOk());

        User driverAfter = userRepository.findById(driver.getId()).orElseThrow();
        assertEquals(BigDecimal.valueOf(8.00).setScale(2, RoundingMode.CEILING), driverAfter.getBalance());

        Rental rental = rentalRepository.findByOrderIdAndBookId(order.getId(), savedBook.getId()).orElseThrow();
        assertEquals(user.getId(), rental.getUser().getId());
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverDeliversOrderWhenWrongStatus_thenThrowException() throws Exception {

        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        CoordinateDTO location = new CoordinateDTO(10.0, 10.0);

        String imagePath = "src/test/resources/orderControllerTest/example_delivery_photo.png";
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        DeliverOrderRequestDTO requestDTO = new DeliverOrderRequestDTO(
                location,
                base64Image
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/deliver")
                        .content(new ObjectMapper().writeValueAsString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverDeliversOrderWhenDifferentDriver_thenThrowException() throws Exception {

        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        User driver2 = new User();
        driver2.setId(userIdGeneratorService.generateUniqueId());
        driver2.setEmail("testdriver2@ocit.com");
        driver2.setRole(driverRole);
        driver2.setPassword(passwordEncoder.encode("password"));
        driver2 = userRepository.save(driver2);

        Order order = new Order();
        order.setDriver(driver2);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        CoordinateDTO location = new CoordinateDTO(10.0, 10.0);

        String imagePath = "src/test/resources/orderControllerTest/example_delivery_photo.png";
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        DeliverOrderRequestDTO requestDTO = new DeliverOrderRequestDTO(
                location,
                base64Image
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/deliver")
                        .content(new ObjectMapper().writeValueAsString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverDeliversOrderWhenTooFarAway_thenThrowException() throws Exception {

        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        CoordinateDTO location = new CoordinateDTO(15.0, 15.0);

        String imagePath = "src/test/resources/orderControllerTest/example_delivery_photo.png";
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        DeliverOrderRequestDTO requestDTO = new DeliverOrderRequestDTO(
                location,
                base64Image
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/deliver")
                        .content(new ObjectMapper().writeValueAsString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverRequestsNavigationToPickup_thenReturnOkAndNavigationResponse() throws Exception {

        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        DeliveryNavigationRequestDTO navigationRequestDTO = new DeliveryNavigationRequestDTO(
                "CAR",
                53.432285,
                14.543989
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/pickup-navigation")
                        .content(new ObjectMapper().writeValueAsString(navigationRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistance").value(0.7));
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverRequestsNavigationToDelivery_thenReturnOkAndNavigationResponse() throws Exception {

        Order order = new Order();
        order.setDriver(driver);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        DeliveryNavigationRequestDTO navigationRequestDTO = new DeliveryNavigationRequestDTO(
                "CAR",
                53.432285,
                14.543989
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/delivery-navigation")
                        .content(new ObjectMapper().writeValueAsString(navigationRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistance").value(1.3));
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverRequestsNavigationToDeliveryOrderIdNotValid_thenReturnNotFound() throws Exception {

        DeliveryNavigationRequestDTO navigationRequestDTO = new DeliveryNavigationRequestDTO(
                "CAR",
                53.432285,
                14.543989
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + 99999999 + "/delivery-navigation")
                        .content(new ObjectMapper().writeValueAsString(navigationRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverRequestsNavigationToDeliveryOrderNotHis_thenReturnNotFound() throws Exception {

        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        User driver2 = new User();
        driver2.setId(userIdGeneratorService.generateUniqueId());
        driver2.setEmail("testdriver2@ocit.com");
        driver2.setRole(driverRole);
        driver2.setPassword(passwordEncoder.encode("password"));
        driver2 = userRepository.save(driver2);

        Order order = new Order();
        order.setDriver(driver2);
        order.setUser(user);
        order.setLibrary(library);
        order.setPickupAddress(library.getAddress());
        order.setDestinationAddress(address);
        order.setIsReturn(false);
        order.setStatus(OrderStatus.DRIVER_ACCEPTED);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        DeliveryNavigationRequestDTO navigationRequestDTO = new DeliveryNavigationRequestDTO(
                "CAR",
                53.432285,
                14.543989
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/delivery-navigation")
                        .content(new ObjectMapper().writeValueAsString(navigationRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private Address createAddress(String postalCode, String city, String street, double lat, double lon) {
        Address address = new Address();
        address.setPostalCode(postalCode);
        address.setCity(city);
        address.setStreet(street);
        address.setLatitude(BigDecimal.valueOf(lat));
        address.setLongitude(BigDecimal.valueOf(lon));
        return addressRepository.save(address);
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

    @Test
    @WithMockUser(username = "testdriver@ocit.com:driver", roles = {"driver"})
    void whenDriverDeliversReturnOrder_thenReturnOkAndCreateRentalReturn() throws Exception {

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

        Order returnOrder = createOrder(user, driver, library1, address, book, 2, OrderStatus.IN_TRANSIT, true);

        RentalReturn rentalReturn = new RentalReturn();
        rentalReturn.setReturnOrder(returnOrder);
        rentalReturn.setStatus(RentalReturnStatus.IN_PROGRESS);

        RentalReturnItem rentalReturnItem = new RentalReturnItem();
        rentalReturnItem.setRentalReturn(rentalReturn);
        rentalReturnItem.setRental(rental);
        rentalReturnItem.setReturnedQuantity(2);

        List<RentalReturnItem> rentalReturnItems = new ArrayList<>();
        rentalReturnItems.add(rentalReturnItem);
        rentalReturn.setRentalReturnItems(rentalReturnItems);

        rentalReturnRepository.save(rentalReturn);

        CoordinateDTO location = new CoordinateDTO(53.423, 14.553);

        String imagePath = "src/test/resources/orderControllerTest/example_delivery_photo.png";
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        DeliverOrderRequestDTO requestDTO = new DeliverOrderRequestDTO(
                location,
                base64Image
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + returnOrder.getId() + "/deliver")
                        .content(new ObjectMapper().writeValueAsString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isPayoutProcessed").value(false))
                .andExpect(status().isOk());

        Order orderAfter = orderRepository.findById(returnOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.AWAITING_LIBRARY_CONFIRMATION, orderAfter.getStatus());
    }
}
