package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.ShoppingCart;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.RoleRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.AuthService;
import edu.zut.bookrider.security.JwtService;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserIdGeneratorService userIdGeneratorService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userIdGeneratorService = mock(UserIdGeneratorService.class);

        authService = new AuthService(authenticationManager, jwtService, userRepository, roleRepository, userIdGeneratorService, passwordEncoder);
    }

    @Test
    public void whenValidUserInput_thenReturnCreatedAccountDTO() {
        CreateUserDTO createUserDTO = new CreateUserDTO("user@example.com", "password");

        when(userRepository.existsByEmailAndRoleName(createUserDTO.getEmail(), "user")).thenReturn(false);
        when(userIdGeneratorService.generateUniqueId()).thenReturn("testId");
        when(passwordEncoder.encode(createUserDTO.getPassword())).thenReturn("encodedPassword");

        Role userRole = new Role("user");

        when(roleRepository.findByName("user")).thenReturn(Optional.of(userRole));

        ShoppingCart shoppingCart = new ShoppingCart();

        User createdUser = new User();
        createdUser.setId("testId");
        createdUser.setRole(userRole);
        createdUser.setEmail("user@example.com");
        createdUser.setPassword("encodedPassword");
        createdUser.setShoppingCart(shoppingCart);
        shoppingCart.setUser(createdUser);

        when(userRepository.save(any())).thenReturn(createdUser);

        CreateAccountResponseDTO response = authService.createUser(createUserDTO);

        assertNotNull(response);
        assertEquals("testId", response.getId());
        assertEquals(createUserDTO.getEmail(), response.getEmail());
        assertNull(response.getUsername());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
    }

    @Test
    public void whenUserEmailTakenInput_thenThrowException() {
        CreateUserDTO createUserDTO = new CreateUserDTO("user@example.com", "password");

        when(userRepository.existsByEmailAndRoleName("user@example.com", "user")).thenReturn(true);

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createUser(createUserDTO)
        );

        assertEquals("Email: 'user@example.com' is already taken for the 'user' role.", thrownException.getMessage());
    }

    @Test
    public void whenValidLibrarianInput_thenReturnCreatedLibrarianDTO() {
        CreateLibrarianDTO createLibrarianDTO = new CreateLibrarianDTO("librarian1", "Adam", "Smith");
        String libraryAdminEmail = "library_admin@example.com";

        Library library = new Library();
        User libraryAdmin = new User();
        libraryAdmin.setLibrary(library);

        when(userRepository.findByEmailAndRoleName(libraryAdminEmail, "library_administrator")).thenReturn(Optional.of(libraryAdmin));
        when(userRepository.existsByUsernameAndLibrary(createLibrarianDTO.getUsername(), library)).thenReturn(false);

        when(userIdGeneratorService.generateUniqueId()).thenReturn("testId");
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        Role librarianRole = new Role("librarian");

        when(roleRepository.findByName("librarian")).thenReturn(Optional.of(librarianRole));

        User createdLibrarian = new User();
        createdLibrarian.setId("testId");
        createdLibrarian.setRole(librarianRole);
        createdLibrarian.setUsername("librarian1");
        createdLibrarian.setPassword("encodedPassword");
        createdLibrarian.setFirstName("Adam");
        createdLibrarian.setLastName("Smith");

        // any() in here, because it didn't want to detect it for some
        // reason, even when they have exactly the same data
        when(userRepository.save(any())).thenReturn(createdLibrarian);

        CreateLibrarianResponseDTO response = authService.createLibrarian(createLibrarianDTO, libraryAdminEmail);

        assertNotNull(response);
        assertEquals("testId", response.getId());
        assertEquals("librarian1", response.getUsername());
        assertEquals("Adam", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertNotNull(response.getTempPassword());
    }

    @Test
    public void whenLibrarianAdminEmailIsWrong_thenThrowException() {
        CreateLibrarianDTO createLibrarianDTO = new CreateLibrarianDTO("librarian1", "adam", "smith");
        String libraryAdminEmail = "library_admin@example.com";

        when(userRepository.findByEmailAndRoleName(libraryAdminEmail, "library_administrator")).thenReturn(Optional.empty());

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createLibrarian(createLibrarianDTO, libraryAdminEmail)
        );

        assertEquals("Library admin with the provided email doesn't exist", thrownException.getMessage());
    }

    @Test
    public void whenLibrarianUsernameTakenWithinLibrary_thenThrowException() {
        CreateLibrarianDTO createLibrarianDTO = new CreateLibrarianDTO("librarian1", "Adam", "Smith");
        String libraryAdminEmail = "library_admin@example.com";

        Library library = new Library();
        User libraryAdmin = new User();
        libraryAdmin.setLibrary(library);

        when(userRepository.findByEmailAndRoleName(libraryAdminEmail, "library_administrator")).thenReturn(Optional.of(libraryAdmin));
        when(userRepository.existsByUsernameAndLibrary(createLibrarianDTO.getUsername(), library)).thenReturn(true);

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createLibrarian(createLibrarianDTO, libraryAdminEmail)
        );

        assertEquals("A librarian with the username '" + createLibrarianDTO.getUsername() + "' already exists in this library", thrownException.getMessage());
    }

    @Test
    public void whenValidLibraryAdminInput_thenReturnCreatedAccountDTO() {
        CreateAdvancedAccountDTO createAdvancedAccountDTO = new CreateAdvancedAccountDTO("library_admin@example.com", "Adam", "Smith" ,"password");

        when(userRepository.existsByEmailAndRoleName(createAdvancedAccountDTO.getEmail(), "library_administrator")).thenReturn(false);
        when(userIdGeneratorService.generateUniqueId()).thenReturn("testId");
        when(passwordEncoder.encode(createAdvancedAccountDTO.getPassword())).thenReturn("encodedPassword");

        Role libraryAdministratorRole = new Role("library_administrator");

        when(roleRepository.findByName("library_administrator")).thenReturn(Optional.of(libraryAdministratorRole));

        User createdLibraryAdmin = new User();
        createdLibraryAdmin.setId("testId");
        createdLibraryAdmin.setRole(libraryAdministratorRole);
        createdLibraryAdmin.setEmail("library_admin@example.com");
        createdLibraryAdmin.setFirstName("Adam");
        createdLibraryAdmin.setLastName("Smith");
        createdLibraryAdmin.setPassword("encodedPassword");

        when(userRepository.save(createdLibraryAdmin)).thenReturn(createdLibraryAdmin);

        CreateAccountResponseDTO response = authService.createAdvancedAccount(createAdvancedAccountDTO, "library_administrator");

        assertNotNull(response);
        assertEquals("testId", response.getId());
        assertEquals(createAdvancedAccountDTO.getEmail(), response.getEmail());
        assertEquals(createAdvancedAccountDTO.getFirstName(), response.getFirstName());
        assertEquals(createAdvancedAccountDTO.getLastName(), response.getLastName());
        assertNull(response.getUsername());
    }

    @Test
    public void whenLibraryAdminEmailTakenInput_thenThrowException() {
        CreateAdvancedAccountDTO createAdvancedAccountDTO = new CreateAdvancedAccountDTO("library_admin@example.com", "Adam", "Smith" ,"password");

        when(userRepository.existsByEmailAndRoleName(createAdvancedAccountDTO.getEmail(), "library_administrator")).thenReturn(true);

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createAdvancedAccount(createAdvancedAccountDTO, "library_administrator")
        );

        assertEquals("Email: '" + createAdvancedAccountDTO.getEmail() + "' is already taken for the 'library_administrator' role.", thrownException.getMessage());
    }

    @Test
    public void whenValidDriverInput_thenReturnCreatedAccountDTO() {
        CreateAdvancedAccountDTO createAdvancedAccountDTO = new CreateAdvancedAccountDTO("driver@example.com", "Adam", "Smith" ,"password");

        when(userRepository.existsByEmailAndRoleName(createAdvancedAccountDTO.getEmail(), "driver")).thenReturn(false);
        when(userIdGeneratorService.generateUniqueId()).thenReturn("testId");
        when(passwordEncoder.encode(createAdvancedAccountDTO.getPassword())).thenReturn("encodedPassword");

        Role driverRole = new Role("driver");

        when(roleRepository.findByName("driver")).thenReturn(Optional.of(driverRole));

        User createdDriver = new User();
        createdDriver.setId("testId");
        createdDriver.setRole(driverRole);
        createdDriver.setEmail("driver@example.com");
        createdDriver.setFirstName("Adam");
        createdDriver.setLastName("Smith");
        createdDriver.setPassword("encodedPassword");

        when(userRepository.save(createdDriver)).thenReturn(createdDriver);

        CreateAccountResponseDTO response = authService.createAdvancedAccount(createAdvancedAccountDTO, "driver");

        assertNotNull(response);
        assertEquals("testId", response.getId());
        assertEquals(createAdvancedAccountDTO.getEmail(), response.getEmail());
        assertEquals(createAdvancedAccountDTO.getFirstName(), response.getFirstName());
        assertEquals(createAdvancedAccountDTO.getLastName(), response.getLastName());
        assertNull(response.getUsername());
    }

    @Test
    public void whenDriverEmailTakenInput_thenThrowException() {
        CreateAdvancedAccountDTO createAdvancedAccountDTO = new CreateAdvancedAccountDTO("driver@example.com", "Adam", "Smith" ,"password");

        when(userRepository.existsByEmailAndRoleName(createAdvancedAccountDTO.getEmail(), "driver")).thenReturn(true);

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createAdvancedAccount(createAdvancedAccountDTO, "driver")
        );

        assertEquals("Email: '" + createAdvancedAccountDTO.getEmail() + "' is already taken for the 'driver' role.", thrownException.getMessage());
    }
}
