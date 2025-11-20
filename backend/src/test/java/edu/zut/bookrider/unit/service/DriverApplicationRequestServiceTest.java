package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.model.DriverApplicationRequest;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.DocumentType;
import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import edu.zut.bookrider.repository.DriverApplicationRequestRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.websocket.WebSocketHandler;
import edu.zut.bookrider.service.DriverApplicationRequestService;
import edu.zut.bookrider.service.DriverDocumentService;
import edu.zut.bookrider.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private WebSocketHandler webSocketHandler;

    @Mock
    private UserService userService;

    private User driver;
    private User systemAdmin;
    private DriverApplicationRequest applicationRequest;
    private CreateDriverDocumentDTO documentDto;

    @BeforeEach
    void setUp() {
        Role driverRole = new Role();
        driverRole.setId(1);
        driverRole.setName("driver");

        Role systemAdministratorRole = new Role();
        systemAdministratorRole.setId(2);
        systemAdministratorRole.setName("system_administrator");

        driver = new User();
        driver.setId("RANDOM");
        driver.setEmail("driver@email.com");
        driver.setRole(driverRole);

        systemAdmin = new User();
        systemAdmin.setId("RANDOM2");
        systemAdmin.setEmail("system_admin@email.com");
        systemAdmin.setRole(systemAdministratorRole);

        applicationRequest = new DriverApplicationRequest();
        applicationRequest.setId(1);
        applicationRequest.setUser(driver);
        applicationRequest.setStatus(DriverApplicationStatus.PENDING);

        documentDto = new CreateDriverDocumentDTO();
        documentDto.setDocumentType(DocumentType.DRIVER_LICENSE);
        documentDto.setExpiryDate(LocalDate.now().plusYears(5));
        documentDto.setImageInBytes(new byte[0]);
    }

    @Test
    void whenValidInputData_thenReturnCreatedDTO() {
        when(authentication.getName()).thenReturn("driver@email.com:driver");
        when(userRepository.findByEmailAndRoleName("driver@email.com", "driver")).thenReturn(java.util.Optional.of(driver));
        when(driverApplicationRequestRepository.existsByUserIdAndPendingOrUnderReview(driver.getId())).thenReturn(false);

        when(driverApplicationRequestRepository.save(any(DriverApplicationRequest.class))).thenReturn(applicationRequest);

        String documentUrl = "http://example.com/driver-license.jpg";
        when(driverDocumentService.saveDriverDocument(documentDto, applicationRequest)).thenReturn(
                new CreateDriverDocumentResponseDTO(DocumentType.DRIVER_LICENSE, documentUrl, LocalDate.now().plusYears(5)));

        when(userService.getAllAdministrators()).thenReturn(List.of(systemAdmin));

        CreateDriverApplicationResponseDTO response = driverApplicationRequestService.createDriverApplication(authentication, List.of(documentDto));

        assertEquals(1, response.getId());
        assertEquals("driver@email.com", response.getEmail());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getCreatedDocuments().size());
        assertEquals(DocumentType.DRIVER_LICENSE, response.getCreatedDocuments().get(0).getDocumentType());
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
    void whenGetApplicationDetailsForAdmin_thenReturnDetails() {
        when(driverApplicationRequestRepository.findById(applicationRequest.getId()))
                .thenReturn(Optional.of(applicationRequest));
        when(authentication.getName()).thenReturn("admin@example.com:system_administrator");

        DriverApplicationDetailsDTO result = driverApplicationRequestService.getApplicationDetails(applicationRequest.getId(), authentication);

        assertNotNull(result);
        assertEquals(applicationRequest.getId(), result.getId());
    }

    @Test
    void whenGetApplicationDetailsForUnauthorizedDriver_thenThrowException() {
        when(driverApplicationRequestRepository.findById(applicationRequest.getId()))
                .thenReturn(Optional.of(applicationRequest));
        when(authentication.getName()).thenReturn("otherdriver@example.com:driver");
        when(userRepository.findByEmailAndRoleName("otherdriver@example.com", "driver"))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            driverApplicationRequestService.getApplicationDetails(applicationRequest.getId(), authentication);
        });
        assertEquals("Application with ID " + applicationRequest.getId() + " doesn't belong to the user", exception.getMessage());
    }

    @Test
    void whenChangeStatus_thenStatusChangedSuccessfully() {
        User admin = new User();
        admin.setId("RANDOM_SOMETHING");
        when(authentication.getName()).thenReturn("admin@example.com:system_administrator");
        when(userRepository.findByEmailAndRoleName("admin@example.com", "system_administrator"))
                .thenReturn(Optional.of(admin));
        when(driverApplicationRequestRepository.findById(applicationRequest.getId()))
                .thenReturn(Optional.of(applicationRequest));

        doNothing().when(webSocketHandler).sendRefreshSignal(any(), any());

        driverApplicationRequestService.changeStatus(applicationRequest.getId(), DriverApplicationStatus.UNDER_REVIEW, null, authentication);

        assertEquals(DriverApplicationStatus.UNDER_REVIEW, applicationRequest.getStatus());
        verify(driverApplicationRequestRepository).save(applicationRequest);
    }

    @Test
    void whenChangeStatusWithoutReason_thenThrowException() {
        when(authentication.getName()).thenReturn("admin@example.com:system_administrator");
        when(userRepository.findByEmailAndRoleName("admin@example.com", "system_administrator"))
                .thenReturn(Optional.of(new User()));
        when(driverApplicationRequestRepository.findById(applicationRequest.getId()))
                .thenReturn(Optional.of(applicationRequest));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            driverApplicationRequestService.changeStatus(applicationRequest.getId(), DriverApplicationStatus.REJECTED, null, authentication);
        });
        assertEquals("Rejection reason must be provided when rejecting an application", exception.getMessage());
    }
}
