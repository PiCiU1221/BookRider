package edu.zut.bookrider.integration.controller;

import edu.zut.bookrider.model.*;
import edu.zut.bookrider.model.enums.OrderStatus;
import edu.zut.bookrider.model.enums.PaymentStatus;
import edu.zut.bookrider.repository.*;
import edu.zut.bookrider.service.UserIdGeneratorService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private User user;
    private User librarian;
    private Library library;
    private Address address;

    @BeforeEach
    void setUp() {

        address = new Address();
        address.setPostalCode("12312");
        address.setCity("Szczecin");
        address.setStreet("Wyszynskiego 10");
        address.setLatitude(BigDecimal.valueOf(10.0));
        address.setLongitude(BigDecimal.valueOf(10.0));
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
    }

    @Test
    @WithMockUser(username = "testuser@ocit.com:user", roles = {"user"})
    void whenUserRequestsOrders_thenReturnUserOrders() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setStatus(OrderStatus.PENDING);
        order.setTargetAddress(address);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setTargetAddress(address);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeOrders").isArray())
                .andExpect(jsonPath("$.activeOrders", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.completedOrders").isArray())
                .andExpect(jsonPath("$.completedOrders", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianRequestsOrders_thenReturnLibraryOrders() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setStatus(OrderStatus.PENDING);
        order.setTargetAddress(address);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setLibrary(library);
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setTargetAddress(address);
        order2.setAmount(BigDecimal.valueOf(10));
        order2.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/librarian"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeOrders").isArray())
                .andExpect(jsonPath("$.activeOrders", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.completedOrders").isArray())
                .andExpect(jsonPath("$.completedOrders", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianAcceptsOrder_thenStatusIsAccepted() throws Exception {

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setStatus(OrderStatus.PENDING);
        order.setTargetAddress(address);
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
        order.setStatus(OrderStatus.ACCEPTED);
        order.setTargetAddress(address);
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
        order.setStatus(OrderStatus.PENDING);
        order.setTargetAddress(address);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/orders/" + order.getId() + "/decline"))
                .andExpect(status().isOk());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.DECLINED, updatedOrder.getStatus());
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenLibrarianHandoverOrderToDriver_thenStatusIsUpdated() throws Exception {

        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        User driver = new User();
        driver.setId(userIdGeneratorService.generateUniqueId());
        driver.setRole(driverRole);
        driver.setEmail("testdriver@ocit.com");
        driver.setFirstName("Adam");
        driver.setLastName("Driver");
        driver.setPassword(passwordEncoder.encode("password"));
        driver = userRepository.save(driver);

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setTargetAddress(address);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDriver(driver);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/handover")
                        .param("driverId", driver.getId()))
                .andExpect(status().isOk());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.IN_TRANSIT, updatedOrder.getStatus());
    }

    @Test
    @WithMockUser(username = "testlibrarian1:1", roles = {"librarian"})
    void whenOrderIsNotAccepted_thenIllegalStateExceptionIsThrown() throws Exception {

        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        User driver = new User();
        driver.setId(userIdGeneratorService.generateUniqueId());
        driver.setRole(driverRole);
        driver.setEmail("testdriver@ocit.com");
        driver.setFirstName("Adam");
        driver.setLastName("Driver");
        driver.setPassword(passwordEncoder.encode("password"));
        driver = userRepository.save(driver);

        Order order = new Order();
        order.setUser(user);
        order.setLibrary(library);
        order.setStatus(OrderStatus.PENDING);
        order.setTargetAddress(address);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDriver(driver);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/handover")
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
        order.setStatus(OrderStatus.ACCEPTED);
        order.setTargetAddress(address);
        order.setAmount(BigDecimal.valueOf(10));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setDriver(driver1);
        order = orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + order.getId() + "/handover")
                        .param("driverId", driver2.getId()))
                .andExpect(status().isBadRequest());
    }
}
