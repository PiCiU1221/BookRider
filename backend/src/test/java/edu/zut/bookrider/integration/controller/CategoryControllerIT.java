package edu.zut.bookrider.integration.controller;

import edu.zut.bookrider.model.*;
import edu.zut.bookrider.repository.CategoryRepository;
import edu.zut.bookrider.repository.RoleRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.hamcrest.Matchers;
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

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserIdGeneratorService userIdGeneratorService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

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

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("ccit_user@bookrider.pl");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(userRole);
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "ccit_user@bookrider.pl:user", roles = {"user"})
    void whenUserGetsAllCategories_thenReturnAllCategories() throws Exception {

        createCategory("Poetry");
        createCategory("Drama");
        createCategory("Horror");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }
}
