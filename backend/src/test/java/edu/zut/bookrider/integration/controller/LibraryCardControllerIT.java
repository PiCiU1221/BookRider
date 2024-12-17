package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.LibraryCardDTO;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.LibraryCard;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.LibraryCardRepository;
import edu.zut.bookrider.repository.LibraryRepository;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class LibraryCardControllerIT {

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
    private ObjectMapper objectMapper;

    @Autowired
    private LibraryCardRepository libraryCardRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    private String userId;
    private Integer cardId;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("example_user@lccit.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();

        User otherUser = new User();
        otherUser.setId(userIdGeneratorService.generateUniqueId());
        otherUser.setEmail("example_user2@lccit.com");
        otherUser.setRole(userRole);
        otherUser.setPassword(passwordEncoder.encode("password"));
        userRepository.save(otherUser);

        Library library = libraryRepository.findById(1).orElseThrow();

        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        User librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setUsername("example_librarian");
        librarian.setRole(librarianRole);
        librarian.setLibrary(library);
        librarian.setPassword(passwordEncoder.encode("password"));
        userRepository.save(librarian);

        LibraryCard libraryCard = new LibraryCard();
        libraryCard.setUser(savedUser);
        libraryCard.setCardId("12345");
        libraryCard.setFirstName("FirstName");
        libraryCard.setLastName("LastName");
        libraryCard.setExpirationDate(LocalDate.of(2028, 6, 29));
        LibraryCard savedLibraryCard = libraryCardRepository.save(libraryCard);
        cardId = savedLibraryCard.getId();
    }

    @Test
    @WithMockUser(username = "example_librarian@lccit.com", roles = "librarian")
    void whenValidData_thenReturnCreatedCardDto() throws Exception {

        LibraryCardDTO libraryCardDTO = new LibraryCardDTO();
        libraryCardDTO.setUserId(userId);
        libraryCardDTO.setCardId("123132");
        libraryCardDTO.setFirstName("Adam");
        libraryCardDTO.setLastName("Mickiewicz");
        libraryCardDTO.setExpirationDate(LocalDate.of(2028, 6, 29));

        String jsonBody = objectMapper.writeValueAsString(libraryCardDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/library-cards")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "example_librarian@lccit.com", roles = "librarian")
    void whenValidId_thenReturnNoContent() throws Exception {

        boolean before = libraryCardRepository.existsById(cardId);
        assertTrue(before);

       mockMvc.perform(MockMvcRequestBuilders.delete("/api/library-cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        boolean after = libraryCardRepository.existsById(cardId);
        assertFalse(after);
    }

    @Test
    @WithMockUser(username = "example_librarian@lccit.com", roles = "librarian")
    void whenValidId_thenReturnOk() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/library-cards/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "example_user2@lccit.com", roles = "user")
    void whenInvalidId_thenReturnBadRequest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/library-cards/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "example_user@lccit.com", roles = "user")
    void whenUserValidId_thenReturnOk() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/library-cards/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }
}
