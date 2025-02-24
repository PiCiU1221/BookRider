package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.ChangePasswordDto;
import edu.zut.bookrider.dto.IsVerifiedResponseDto;
import edu.zut.bookrider.dto.UserIdResponseDto;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.LibraryRepository;
import edu.zut.bookrider.repository.RoleRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserIdGeneratorService userIdGeneratorService;

    @Autowired
    private ObjectMapper objectMapper;

    private User userReference;
    @Autowired
    private LibraryRepository libraryRepository;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setRole(userRole);
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("example@usit.com");
        user.setPassword(passwordEncoder.encode("password"));
        userReference = userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "example@usit.com", roles = {"user"})
    void whenUserValid_thenReturnUserIdDto() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        UserIdResponseDto responseDto = new ObjectMapper().readValue(responseBody, UserIdResponseDto.class);

        assertEquals(userReference.getId(), responseDto.getUserId());
    }

    @Test
    @WithMockUser(username = "example@usit.com", roles = {"user"})
    void whenUserIsValid_thenReturnUserIsVerifiedDto() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/is-verified")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        IsVerifiedResponseDto responseDto = new ObjectMapper().readValue(responseBody, IsVerifiedResponseDto.class);

        assertEquals(userReference.getIsVerified(), responseDto.getIsVerified());
    }

    @Test
    @WithMockUser(username = "example@usit.com", roles = {"user"})
    void whenUserIsValid_thenChangeUserPasswordAndReturnNoContent() throws Exception {

        String newPassword = "newPassword";
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("password", newPassword);

        String jsonBody = objectMapper.writeValueAsString(changePasswordDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/change-password")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        User user = userRepository.findById(userReference.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userReference.getId()));

        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
    }

    @Test
    @WithMockUser(username = "example@usit.com", roles = {"user"})
    void whenUserIsValidWithIncorrectOldPassword_thenReturnIsUnauthorized() throws Exception {

        String newPassword = "newPassword";
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("wrongPassword", newPassword);

        String jsonBody = objectMapper.writeValueAsString(changePasswordDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/change-password")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Nested
    class UserControllerProfileEndpointIT {

        @BeforeEach
        void setup() {

            User librarian = new User();
            librarian.setId(userIdGeneratorService.generateUniqueId());
            Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
            librarian.setRole(librarianRole);
            Library library = libraryRepository.findById(1).orElseThrow();
            librarian.setLibrary(library);
            librarian.setUsername("testLibrarian");
            librarian.setPassword("password");
            librarian.setFirstName("Adam");
            librarian.setLastName("Mickiewicz");
            userRepository.save(librarian);

            User driver = new User();
            driver.setId(userIdGeneratorService.generateUniqueId());
            Role driverRole = roleRepository.findByName("driver").orElseThrow();
            driver.setRole(driverRole);
            driver.setEmail("testDriver@ucit.com");
            driver.setPassword("password");
            driver.setFirstName("Adam");
            driver.setLastName("Mickiewicz");
            driver.setBalance(BigDecimal.valueOf(110));
            userRepository.save(driver);

            User user = new User();
            user.setId(userIdGeneratorService.generateUniqueId());
            Role userRole = roleRepository.findByName("user").orElseThrow();
            user.setRole(userRole);
            user.setEmail("testUser@ucit.com");
            user.setPassword("password");
            user.setBalance(BigDecimal.valueOf(100));
            userRepository.save(user);
        }

        @Test
        @WithMockUser(username = "testLibrarian:1", roles = {"librarian"})
        void whenLibrarianRequestsHisProfile_thenReturnOkAndHisProfile() throws Exception {

            mockMvc.perform(MockMvcRequestBuilders.get("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testLibrarian"))
                    .andExpect(jsonPath("$.email").value((Object) null))
                    .andExpect(jsonPath("$.firstName").value("Adam"))
                    .andExpect(jsonPath("$.lastName").value("Mickiewicz"));
        }

        @Test
        @WithMockUser(username = "testDriver@ucit.com", roles = {"driver"})
        void whenDriverRequestsHisProfile_thenReturnOkAndHisProfile() throws Exception {

            mockMvc.perform(MockMvcRequestBuilders.get("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value((Object) null))
                    .andExpect(jsonPath("$.email").value("testDriver@ucit.com"))
                    .andExpect(jsonPath("$.firstName").value("Adam"))
                    .andExpect(jsonPath("$.lastName").value("Mickiewicz"))
                    .andExpect(jsonPath("$.balance").value(110));
        }

        @Test
        @WithMockUser(username = "testUser@ucit.com", roles = {"user"})
        void whenUserRequestsHisProfile_thenReturnOkAndHisProfile() throws Exception {

            mockMvc.perform(MockMvcRequestBuilders.get("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value((Object) null))
                    .andExpect(jsonPath("$.email").value("testUser@ucit.com"))
                    .andExpect(jsonPath("$.firstName").value((Object) null))
                    .andExpect(jsonPath("$.lastName").value((Object) null))
                    .andExpect(jsonPath("$.balance").value(100));
        }
    }
}
