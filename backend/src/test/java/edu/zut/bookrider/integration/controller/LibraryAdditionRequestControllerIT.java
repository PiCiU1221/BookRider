package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CreateLibraryAdditionDTO;
import edu.zut.bookrider.exception.LibraryRequestNotFoundException;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.LibraryAdditionRequest;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import edu.zut.bookrider.repository.AddressRepository;
import edu.zut.bookrider.repository.LibraryAdditionRequestRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class LibraryAdditionRequestControllerIT {

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
    private AddressRepository addressRepository;
    @Autowired
    private LibraryAdditionRequestRepository libraryAdditionRequestRepository;

    private User userReference;
    private Address addressReference;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("library_administrator").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("library_administator@gmail.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        userReference = userRepository.save(user);

        Role systemAdminRole = roleRepository.findByName("system_administrator").orElseThrow();
        User system_admin = new User();
        system_admin.setId(userIdGeneratorService.generateUniqueId());
        system_admin.setEmail("system_administator@gmail.com");
        system_admin.setRole(systemAdminRole);
        system_admin.setPassword(passwordEncoder.encode("password"));
        userRepository.save(system_admin);

        Address address = new Address();
        address.setPostalCode("12312");
        address.setCity("Szczecin");
        address.setStreet("Wyszynskiego 10");
        address.setLatitude(BigDecimal.valueOf(10.0));
        address.setLongitude(BigDecimal.valueOf(10.0));
        addressReference = addressRepository.save(address);
    }

    @Test
    @WithMockUser(username = "library_administator@gmail.com", roles = {"library_administrator"})
    void whenValidInput_thenReturnDTO() throws Exception {
        CreateLibraryAdditionDTO createLibraryAdditionDTO = new CreateLibraryAdditionDTO();
        createLibraryAdditionDTO.setStreet("Wojska Polskiego 14");
        createLibraryAdditionDTO.setCity("Szczecin");
        createLibraryAdditionDTO.setPostalCode("73-123");
        createLibraryAdditionDTO.setLibraryName("Filia nr. 5");
        createLibraryAdditionDTO.setPhoneNumber("123123123");
        createLibraryAdditionDTO.setLibraryEmail("filia5@szczecin.gov.pl");

        String jsonBody = objectMapper.writeValueAsString(createLibraryAdditionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/library-requests")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "system_administator@gmail.com", roles = {"system_administrator"})
    void whenWrongStatus_thenReturnBadRequest() throws Exception {

        LibraryAdditionRequest request = new LibraryAdditionRequest();
        request.setCreatedBy(userReference);
        request.setAddress(addressReference);
        request.setLibraryName("Biblioteka 234231");
        request.setStatus(LibraryAdditionStatus.PENDING);
        LibraryAdditionRequest savedRequest = libraryAdditionRequestRepository.save(request);

        int requestId = savedRequest.getId();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/library-requests/{id}/status", requestId)
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "system_administator@gmail.com", roles = {"system_administrator"})
    void whenValidInput_thenReturnNoContentAndUpdateStatus() throws Exception {

        LibraryAdditionRequest request = new LibraryAdditionRequest();
        request.setCreatedBy(userReference);
        request.setAddress(addressReference);
        request.setLibraryName("Biblioteka 234231");
        request.setStatus(LibraryAdditionStatus.PENDING);
        LibraryAdditionRequest savedRequest = libraryAdditionRequestRepository.save(request);

        int requestId = savedRequest.getId();

        LibraryAdditionRequest beforeUpdate = libraryAdditionRequestRepository.findById(requestId)
                .orElseThrow(() -> new LibraryRequestNotFoundException("Request not found"));

        assertEquals(LibraryAdditionStatus.PENDING, beforeUpdate.getStatus());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/library-requests/{id}/status", requestId)
                        .param("status", "UNDER_REVIEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        LibraryAdditionRequest afterUpdate = libraryAdditionRequestRepository.findById(requestId)
                .orElseThrow(() -> new LibraryRequestNotFoundException("Request not found"));

        assertEquals(LibraryAdditionStatus.UNDER_REVIEW, afterUpdate.getStatus());
    }

    @Test
    @WithMockUser(username = "system_administator@gmail.com", roles = {"system_administrator"})
    void whenValidInput_thenReturnNoContentAndSetReviewedByAndUpdateStatus() throws Exception {

        LibraryAdditionRequest request = new LibraryAdditionRequest();
        request.setCreatedBy(userReference);
        request.setAddress(addressReference);
        request.setLibraryName("Biblioteka 234231");
        request.setStatus(LibraryAdditionStatus.PENDING);
        LibraryAdditionRequest savedRequest = libraryAdditionRequestRepository.save(request);

        int requestId = savedRequest.getId();

        LibraryAdditionRequest beforeUpdate = libraryAdditionRequestRepository.findById(requestId)
                .orElseThrow(() -> new LibraryRequestNotFoundException("Request not found"));

        assertNull(beforeUpdate.getReviewedBy());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/library-requests/{id}/status", requestId)
                        .param("status", "REJECTED")
                        .param("rejectionReason", "I don't like your mustache")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        LibraryAdditionRequest afterUpdate = libraryAdditionRequestRepository.findById(requestId)
                .orElseThrow(() -> new LibraryRequestNotFoundException("Request not found"));


        assertNotNull(afterUpdate.getReviewedBy());
        assertNotNull(afterUpdate.getReviewedAt());
        assertEquals(LibraryAdditionStatus.REJECTED, afterUpdate.getStatus());
    }

    @Test
    @WithMockUser(username = "system_administator@gmail.com", roles = {"system_administrator"})
    void whenValidInput_thenSetReviewedByAndUpdateStatusAndUpdateUserLibrary() throws Exception {

        LibraryAdditionRequest request = new LibraryAdditionRequest();
        request.setCreatedBy(userReference);
        request.setAddress(addressReference);
        request.setLibraryName("Biblioteka 234231");
        request.setStatus(LibraryAdditionStatus.PENDING);
        LibraryAdditionRequest savedRequest = libraryAdditionRequestRepository.save(request);

        int requestId = savedRequest.getId();

        LibraryAdditionRequest beforeUpdate = libraryAdditionRequestRepository.findById(requestId)
                .orElseThrow(() -> new LibraryRequestNotFoundException("Request not found"));

        assertNull(beforeUpdate.getReviewedBy());
        assertNull(userReference.getLibrary());
        assertFalse(userReference.getIsVerified());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/library-requests/{id}/status", requestId)
                        .param("status", "APPROVED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        LibraryAdditionRequest afterUpdate = libraryAdditionRequestRepository.findById(requestId)
                .orElseThrow(() -> new LibraryRequestNotFoundException("Request not found"));

        assertNotNull(afterUpdate.getReviewedBy());
        assertNotNull(afterUpdate.getReviewedAt());
        assertNotNull(userReference.getLibrary());
        assertTrue(userReference.getIsVerified());

        assertEquals(LibraryAdditionStatus.APPROVED, afterUpdate.getStatus());
    }
}
