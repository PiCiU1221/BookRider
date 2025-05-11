package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.dto.CreateLibraryAdditionDTO;
import edu.zut.bookrider.dto.CreateLibraryAdditionResponseDTO;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.LibraryAdditionRequest;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import edu.zut.bookrider.repository.LibraryAdditionRequestRepository;
import edu.zut.bookrider.repository.LibraryRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.websocket.WebSocketHandler;
import edu.zut.bookrider.service.AddressService;
import edu.zut.bookrider.service.LibraryAdditionRequestService;
import edu.zut.bookrider.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class LibraryAdditionRequestServiceTest {

    @InjectMocks
    private LibraryAdditionRequestService libraryAdditionRequestService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private LibraryAdditionRequestRepository libraryAdditionRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private WebSocketHandler webSocketHandler;

    private Authentication authentication;
    private User libraryAdmin;
    private User systemAdmin;
    private CreateLibraryAdditionDTO createLibraryAdditionDTO;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("library_admin@email.com");

        Role libraryAdministratorRole = new Role();
        libraryAdministratorRole.setId(1);
        libraryAdministratorRole.setName("library_administrator");

        Role systemAdministratorRole = new Role();
        systemAdministratorRole.setId(2);
        systemAdministratorRole.setName("system_administrator");

        libraryAdmin = new User();
        libraryAdmin.setId("RANDOM");
        libraryAdmin.setEmail("library_admin@email.com");
        libraryAdmin.setRole(libraryAdministratorRole);

        systemAdmin = new User();
        systemAdmin.setId("RANDOM2");
        systemAdmin.setEmail("system_admin@email.com");
        systemAdmin.setRole(systemAdministratorRole);

        createLibraryAdditionDTO = new CreateLibraryAdditionDTO();
        createLibraryAdditionDTO.setStreet("Wojska Polskiego 14");
        createLibraryAdditionDTO.setCity("Szczecin");
        createLibraryAdditionDTO.setPostalCode("73123");
        createLibraryAdditionDTO.setLibraryName("Filia nr. 5");
        createLibraryAdditionDTO.setPhoneNumber("123123123");
        createLibraryAdditionDTO.setLibraryEmail("filia5@szczecin.gov.pl");
    }

    @Test
    void whenValidInput_thenReturnCreatedDTO() {
        when(userRepository.findByEmailAndRoleName("library_admin@email.com", "library_administrator"))
                .thenReturn(Optional.of(libraryAdmin));

        when(libraryRepository.existsByAddress(createLibraryAdditionDTO.getStreet(),
                createLibraryAdditionDTO.getCity(), createLibraryAdditionDTO.getPostalCode()))
                .thenReturn(false);

        when(libraryRepository.existsByNameAndCity(createLibraryAdditionDTO.getLibraryName(), createLibraryAdditionDTO.getCity()))
                .thenReturn(false);

        when(addressService.findOrCreateAddress(any(CreateAddressDTO.class)))
                .thenReturn(new Address());

        LibraryAdditionRequest savedRequest = new LibraryAdditionRequest();
        savedRequest.setId(1);
        savedRequest.setCreatedBy(libraryAdmin);
        savedRequest.setAddress(new Address("Wojska Polskiego 14", "Szczecin", "73123", BigDecimal.valueOf(1), BigDecimal.valueOf(1)));
        savedRequest.setLibraryName("Filia nr. 5");
        savedRequest.setPhoneNumber("123123123");
        savedRequest.setEmail("filia5@szczecin.gov.pl");
        savedRequest.setStatus(LibraryAdditionStatus.PENDING);

        when(libraryAdditionRequestRepository.save(any(LibraryAdditionRequest.class))).thenReturn(savedRequest);

        when(userService.getAllAdministrators()).thenReturn(List.of(systemAdmin));

        doNothing().when(webSocketHandler).sendRefreshSignal(any(), any());

        CreateLibraryAdditionResponseDTO response = libraryAdditionRequestService.createLibraryRequest(createLibraryAdditionDTO, authentication);

        assertThat(response).isNotNull();
        assertThat(response.getLibraryName()).isEqualTo("Filia nr. 5");
        assertThat(response.getAddress().getStreet()).isEqualTo("Wojska Polskiego 14");
        assertThat(response.getAddress().getCity()).isEqualTo("Szczecin");
        assertThat(response.getAddress().getPostalCode()).isEqualTo("73123");
        assertThat(response.getPhoneNumber()).isEqualTo("123123123");
        assertThat(response.getEmail()).isEqualTo("filia5@szczecin.gov.pl");
    }

    @Test
    void whenAddressAlreadyExists_thenThrowException() {

        when(userRepository.findByEmailAndRoleName("library_admin@email.com", "library_administrator"))
                .thenReturn(Optional.of(libraryAdmin));

        when(libraryRepository.existsByAddress(createLibraryAdditionDTO.getStreet(),
                createLibraryAdditionDTO.getCity(), createLibraryAdditionDTO.getPostalCode()))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                libraryAdditionRequestService.createLibraryRequest(createLibraryAdditionDTO, authentication));
        assertThat(exception.getMessage()).isEqualTo("A library with the same address already exists.");
    }

    @Test
    void whenLibraryWithSameNameAndCityExists_thenThrowException() {
        when(userRepository.findByEmailAndRoleName("library_admin@email.com", "library_administrator"))
                .thenReturn(Optional.of(libraryAdmin));

        when(libraryRepository.existsByAddress(createLibraryAdditionDTO.getStreet(),
                createLibraryAdditionDTO.getCity(), createLibraryAdditionDTO.getPostalCode()))
                .thenReturn(false);

        when(libraryRepository.existsByNameAndCity(createLibraryAdditionDTO.getLibraryName(), createLibraryAdditionDTO.getCity()))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                libraryAdditionRequestService.createLibraryRequest(createLibraryAdditionDTO, authentication));
        assertThat(exception.getMessage()).isEqualTo("A library with the same name already exists in this city.");
    }

    @Test
    void whenUserNotFound_thenThrowException() {
        when(userRepository.findByEmailAndRoleName("library_admin@email.com", "library_administrator"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                libraryAdditionRequestService.createLibraryRequest(createLibraryAdditionDTO, authentication));
        assertThat(exception.getMessage()).isEqualTo("User with the provided email and 'library_administrator' role doesn't exist");
    }
}
