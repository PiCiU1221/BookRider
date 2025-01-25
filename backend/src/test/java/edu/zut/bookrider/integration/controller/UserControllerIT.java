package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.ChangePasswordDto;
import edu.zut.bookrider.dto.IsVerifiedResponseDto;
import edu.zut.bookrider.dto.UserIdResponseDto;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.model.Role;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void whenUserIsValidWithIncorrectOldPassword_thenReturn() throws Exception {

        String newPassword = "newPassword";
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("wrongPassword", newPassword);

        String jsonBody = objectMapper.writeValueAsString(changePasswordDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/change-password")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
