package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CreateDriverDocumentStringDTO;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Arrays;
import java.util.Base64;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class DriverApplicationControllerIT {

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

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("driver").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("driver@gmail.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "driver@gmail.com", roles = {"driver"})
    void whenValidInput_returnDTO() throws Exception {
        File driverLicense = new ClassPathResource("driverApplicationServiceTest/driverLicence.jpg").getFile();
        File identityCard = new ClassPathResource("driverApplicationServiceTest/identityCard.jpg").getFile();

        String driverLicenseString = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(driverLicense.toPath()));
        String identityCardString = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(identityCard.toPath()));

        CreateDriverDocumentStringDTO driverLicenseDTO = new CreateDriverDocumentStringDTO(
                driverLicenseString, "Driving License", "2025-12-31"
        );
        CreateDriverDocumentStringDTO identityCardDTO = new CreateDriverDocumentStringDTO(
                identityCardString, "Identity Card", "2025-12-31"
        );

        String jsonBody = objectMapper.writeValueAsString(Arrays.asList(driverLicenseDTO, identityCardDTO));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/driver-applications")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);
    }
}
