package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CreateDriverDocumentStringDTO;
import edu.zut.bookrider.exception.DriverApplicationNotFoundException;
import edu.zut.bookrider.model.*;
import edu.zut.bookrider.model.enums.DocumentType;
import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import edu.zut.bookrider.repository.DriverApplicationRequestRepository;
import edu.zut.bookrider.repository.RoleRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.junit.jupiter.api.*;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Autowired
    private DriverApplicationRequestRepository driverApplicationRequestRepository;

    private Integer requestId;
    private User driverReference;

    @BeforeEach
    void setUp() {
        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        Role systemAdministratorRole = roleRepository.findByName("system_administrator").orElseThrow();

        User systemAdmin = new User();
        systemAdmin.setId(userIdGeneratorService.generateUniqueId());
        systemAdmin.setEmail("admin@dacit.com");
        systemAdmin.setRole(systemAdministratorRole);
        systemAdmin.setPassword(passwordEncoder.encode("password"));
        userRepository.save(systemAdmin);

        User driver = new User();
        driver.setId(userIdGeneratorService.generateUniqueId());
        driver.setEmail("driver@dacit.com");
        driver.setRole(driverRole);
        driver.setPassword(passwordEncoder.encode("password"));
        userRepository.save(driver);

        User otherDriver = new User();
        otherDriver.setId(userIdGeneratorService.generateUniqueId());
        otherDriver.setEmail("other_driver@dacit.com");
        otherDriver.setRole(driverRole);
        otherDriver.setPassword(passwordEncoder.encode("password"));
        driverReference = userRepository.save(otherDriver);

        DriverApplicationRequest request = new DriverApplicationRequest();
        request.setUser(otherDriver);
        request.setStatus(DriverApplicationStatus.PENDING);
        DriverApplicationRequest savedRequest = driverApplicationRequestRepository.save(request);

        requestId = savedRequest.getId();
    }

    @Test
    @WithMockUser(username = "driver@dacit.com", roles = {"driver"})
    void whenValidInput_returnDTO() throws Exception {
        File driverLicense = new ClassPathResource("driverApplicationServiceTest/driverLicence.jpg").getFile();
        File identityCard = new ClassPathResource("driverApplicationServiceTest/identityCard.jpg").getFile();

        String driverLicenseString = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(driverLicense.toPath()));
        String identityCardString = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(identityCard.toPath()));

        CreateDriverDocumentStringDTO driverLicenseDTO = new CreateDriverDocumentStringDTO(
                driverLicenseString, DocumentType.DRIVER_LICENSE.toString(), "2025-12-31"
        );
        CreateDriverDocumentStringDTO identityCardDTO = new CreateDriverDocumentStringDTO(
                identityCardString, DocumentType.ID.toString(), "2025-12-31"
        );

        String jsonBody = objectMapper.writeValueAsString(Arrays.asList(driverLicenseDTO, identityCardDTO));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/driver-applications")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        //System.out.println("Response Body: " + responseBody);
    }

    @Test
    @WithMockUser(username = "admin@dacit.com", roles = {"system_administrator"})
    void whenListApplicationsAsAdmin_thenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/driver-applications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "driver@dacit.com", roles = {"driver"})
    void whenListMyApplicationsAsDriver_thenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/driver-applications/me")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@dacit.com", roles = {"system_administrator"})
    void whenWrongStatus_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver-applications/{id}/status", requestId)
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@dacit.com", roles = {"system_administrator"})
    void whenValidInput_thenReturnNoContentAndUpdateStatus() throws Exception {

        DriverApplicationRequest beforeUpdate = driverApplicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new DriverApplicationNotFoundException("Request not found"));

        assertEquals(DriverApplicationStatus.PENDING, beforeUpdate.getStatus());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver-applications/{id}/status", requestId)
                        .param("status", "UNDER_REVIEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        DriverApplicationRequest afterUpdate = driverApplicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new DriverApplicationNotFoundException("Request not found"));

        assertEquals(DriverApplicationStatus.UNDER_REVIEW, afterUpdate.getStatus());
    }

    @Test
    @WithMockUser(username = "admin@dacit.com", roles = {"system_administrator"})
    void whenValidInput_thenReturnNoContentAndSetReviewedByAndUpdateStatus() throws Exception {

        DriverApplicationRequest beforeUpdate = driverApplicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new DriverApplicationNotFoundException("Request not found"));

        assertNull(beforeUpdate.getReviewedBy());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver-applications/{id}/status", requestId)
                        .param("status", "REJECTED")
                        .param("rejectionReason", "I don't like your mustache")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        DriverApplicationRequest afterUpdate = driverApplicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new DriverApplicationNotFoundException("Request not found"));

        assertNotNull(afterUpdate.getReviewedBy());
        assertNotNull(afterUpdate.getReviewedAt());
        assertEquals(DriverApplicationStatus.REJECTED, afterUpdate.getStatus());
    }

    @Test
    @WithMockUser(username = "admin@dacit.com", roles = {"system_administrator"})
    void whenValidInput_thenSetReviewedByAndUpdateStatusAndUpdateUserLibrary() throws Exception {

        DriverApplicationRequest beforeUpdate = driverApplicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new DriverApplicationNotFoundException("Request not found"));

        assertNull(beforeUpdate.getReviewedBy());
        assertFalse(driverReference.getIsVerified());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/driver-applications/{id}/status", requestId)
                        .param("status", "APPROVED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        DriverApplicationRequest afterUpdate = driverApplicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new DriverApplicationNotFoundException("Request not found"));

        assertNotNull(afterUpdate.getReviewedBy());
        assertNotNull(afterUpdate.getReviewedAt());
        assertTrue(driverReference.getIsVerified());

        assertEquals(DriverApplicationStatus.APPROVED, afterUpdate.getStatus());
    }
}
