package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.CreateDriverApplicationResponseDTO;
import edu.zut.bookrider.dto.CreateDriverDocumentDTO;
import edu.zut.bookrider.dto.CreateDriverDocumentResponseDTO;
import edu.zut.bookrider.model.DriverApplicationRequest;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import edu.zut.bookrider.repository.DriverApplicationRequestRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.DriverApplicationRequestService;
import edu.zut.bookrider.service.DriverDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class DriverApplicationRequestServiceTest {

    @InjectMocks
    private DriverApplicationRequestService driverApplicationRequestService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverApplicationRequestRepository driverApplicationRequestRepository;

    @Mock
    private DriverDocumentService driverDocumentService;

    @Mock
    private Authentication authentication;

    private User driver;
    private DriverApplicationRequest applicationRequest;
    private CreateDriverDocumentDTO documentDto;

    @BeforeEach
    void setUp() {
        Role driverRole = new Role();
        driverRole.setId(1);
        driverRole.setName("driver");

        driver = new User();
        driver.setId("RANDOM");
        driver.setEmail("driver@email.com");
        driver.setRole(driverRole);

        applicationRequest = new DriverApplicationRequest();
        applicationRequest.setId(1);
        applicationRequest.setUser(driver);
        applicationRequest.setStatus(DriverApplicationStatus.PENDING);

        documentDto = new CreateDriverDocumentDTO();
        documentDto.setDocumentType("License");
        documentDto.setExpiryDate(LocalDate.now().plusYears(5));
        documentDto.setImageInBytes(new byte[0]);
    }

    @Test
    void whenValidInputData_thenReturnCreatedDTO() throws IOException {
        when(authentication.getName()).thenReturn("driver@email.com:driver");
        when(userRepository.findByEmailAndRoleName("driver@email.com", "driver")).thenReturn(java.util.Optional.of(driver));
        when(driverApplicationRequestRepository.existsByUserIdAndPendingOrUnderReview(driver.getId())).thenReturn(false);

        when(driverApplicationRequestRepository.save(any(DriverApplicationRequest.class))).thenReturn(applicationRequest);

        String documentUrl = "http://example.com/license.jpg";
        when(driverDocumentService.saveDriverDocument(documentDto, applicationRequest)).thenReturn(
                new CreateDriverDocumentResponseDTO("License", documentUrl, LocalDate.now().plusYears(5)));

        CreateDriverApplicationResponseDTO response = driverApplicationRequestService.createDriverApplication(authentication, List.of(documentDto));

        assertEquals(1, response.getId());
        assertEquals("driver@email.com", response.getEmail());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getCreatedDocuments().size());
        assertEquals("License", response.getCreatedDocuments().get(0).getDocumentType());
        assertEquals(documentUrl, response.getCreatedDocuments().get(0).getDocumentPhotoUrl());
        assertEquals(LocalDate.now().plusYears(5), response.getCreatedDocuments().get(0).getExpiryDate());
    }

    @Test
    void whenUserAlreadyHasPendingOrUnderReviewRequest_thenThrowException() {
        when(authentication.getName()).thenReturn("driver@email.com:driver");
        when(userRepository.findByEmailAndRoleName("driver@email.com", "driver")).thenReturn(java.util.Optional.of(driver));
        when(driverApplicationRequestRepository.existsByUserIdAndPendingOrUnderReview(driver.getId())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            driverApplicationRequestService.createDriverApplication(authentication, List.of(documentDto));
        });
    }

    @Test
    void whenImageUploadFails_thenThrowException() throws IOException {
        when(authentication.getName()).thenReturn("driver@email.com:driver");
        when(userRepository.findByEmailAndRoleName("driver@email.com", "driver")).thenReturn(java.util.Optional.of(driver));
        when(driverApplicationRequestRepository.existsByUserIdAndPendingOrUnderReview(driver.getId())).thenReturn(false);

        when(driverApplicationRequestRepository.save(any(DriverApplicationRequest.class))).thenReturn(applicationRequest);

        when(driverDocumentService.saveDriverDocument(documentDto, applicationRequest)).thenThrow(new IOException("Upload failed"));

        assertThrows(RuntimeException.class, () -> {
            driverApplicationRequestService.createDriverApplication(authentication, List.of(documentDto));
        });
    }
}
