package edu.zut.bookrider.integration.controller;


import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.ShoppingCart;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.RoleRepository;
import edu.zut.bookrider.repository.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class TransactionControllerIT {

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

    private User userReference;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("testuser@tcit.com");
        user.setRole(userRole);
        user.setBalance(BigDecimal.valueOf(0));
        user.setPassword(passwordEncoder.encode("password"));

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        user.setShoppingCart(shoppingCart);

        userReference = userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "testuser@tcit.com", roles = {"user"})
    void whenValidInput_thenReturnOkAndIncreaseBalance() throws Exception {

        assertEquals(BigDecimal.valueOf(0), userReference.getBalance());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions/deposit")
                        .param("amount", "10.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(BigDecimal.valueOf(10.00).setScale(2, RoundingMode.CEILING), userReference.getBalance());
    }

    @Test
    @WithMockUser(username = "testuser@tcit.com", roles = {"user"})
    void whenInvalidAmount_thenReturnBadRequest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions/deposit")
                        .param("amount", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
