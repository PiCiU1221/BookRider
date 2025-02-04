package edu.zut.bookrider.integration.security;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.AddressRepository;
import edu.zut.bookrider.repository.LibraryRepository;
import edu.zut.bookrider.repository.RoleRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.AuthService;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
public class AuthServiceIT {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserIdGeneratorService userIdGeneratorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void whenValidUserCredentials_thenReturnToken() {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("test@gmail.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setIdentifier("test@gmail.com");
        loginRequestDTO.setPassword("password");

        String resultToken = authService.authenticateBasicAccount(loginRequestDTO, "user");
        assertNotNull(resultToken);

        // To debug
        //System.out.println(resultToken);
    }

    @Test
    void whenInvalidUserCredentials_ThrowException() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setIdentifier("test2@gmail.com");
        loginRequestDTO.setPassword("password2");

        BadCredentialsException thrownException = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateBasicAccount(loginRequestDTO, "user")
        );

        assertEquals("Invalid email or password.", thrownException.getMessage());
    }

    @Test
    void whenValidDriverCredentials_thenReturnToken() {
        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        User driver = new User();
        driver.setId(userIdGeneratorService.generateUniqueId());
        driver.setEmail("test@gmail.com");
        driver.setRole(driverRole);
        driver.setPassword(passwordEncoder.encode("password"));
        userRepository.save(driver);

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setIdentifier("test@gmail.com");
        loginRequestDTO.setPassword("password");

        String resultToken = authService.authenticateBasicAccount(loginRequestDTO, "driver");
        assertNotNull(resultToken);

        // To debug
        //System.out.println(resultToken);
    }

    @Test
    void whenInvalidDriverCredentials_ThrowException() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setIdentifier("test2@gmail.com");
        loginRequestDTO.setPassword("password2");

        BadCredentialsException thrownException = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateBasicAccount(loginRequestDTO, "driver")
        );

        assertEquals("Invalid email or password.", thrownException.getMessage());
    }

    @Test
    void whenValidLibrarianCredentials_thenReturnToken() {
        Address address = new Address();
        address.setStreet("Random");
        address.setCity("Random");
        address.setPostalCode("Random");
        address.setLatitude(BigDecimal.valueOf(10));
        address.setLongitude(BigDecimal.valueOf(10));
        addressRepository.save(address);

        Library library = new Library();
        library.setAddress(address);
        library.setName("Example");
        Library savedLibrary = libraryRepository.save(library);

        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        User librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setUsername("librarian1");
        librarian.setRole(librarianRole);
        librarian.setLibrary(savedLibrary);
        librarian.setPassword(passwordEncoder.encode("password"));
        userRepository.save(librarian);

        LibrarianLoginRequestDTO librarianLoginRequestDTO = new LibrarianLoginRequestDTO();
        librarianLoginRequestDTO.setUsername("librarian1");
        librarianLoginRequestDTO.setLibraryId(savedLibrary.getId());
        librarianLoginRequestDTO.setPassword("password");

        String resultToken = authService.authenticateLibrarian(librarianLoginRequestDTO);
        assertNotNull(resultToken);

        // To debug
        //System.out.println(resultToken);
    }

    @Test
    void whenInvalidLibrarianCredentials_thenThrowException() {
        LibrarianLoginRequestDTO librarianLoginRequestDTO = new LibrarianLoginRequestDTO();
        librarianLoginRequestDTO.setUsername("librarian1");
        librarianLoginRequestDTO.setLibraryId(1);
        librarianLoginRequestDTO.setPassword("password");

        BadCredentialsException thrownException = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateLibrarian(librarianLoginRequestDTO)
        );

        assertEquals("Invalid email or password.", thrownException.getMessage());
    }

    @Test
    public void whenValidUserInput_thenReturnCreatedAccountDTO() {
        CreateUserDTO createUserDTO = new CreateUserDTO("user@example.com", "password");

        CreateAccountResponseDTO response = authService.createUser(createUserDTO);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(createUserDTO.getEmail(), response.getEmail());
        assertNull(response.getUsername());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
    }

    @Test
    public void whenUserEmailTakenInput_thenThrowException() {
        CreateUserDTO createUserDTO = new CreateUserDTO("user@example.com", "password");

        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("user@example.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

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

        Role adminRole = roleRepository.findByName("library_administrator").orElseThrow();

        Address address = new Address();
        address.setStreet("Random");
        address.setCity("Random");
        address.setPostalCode("Random");
        address.setLatitude(BigDecimal.valueOf(10));
        address.setLongitude(BigDecimal.valueOf(10));
        addressRepository.save(address);

        Library library = new Library();
        library.setAddress(address);
        library.setName("Example");
        libraryRepository.save(library);

        User libraryAdmin = new User();
        libraryAdmin.setId(userIdGeneratorService.generateUniqueId());
        libraryAdmin.setEmail(libraryAdminEmail);
        libraryAdmin.setPassword(passwordEncoder.encode("password"));
        libraryAdmin.setRole(adminRole);
        libraryAdmin.setLibrary(library);
        userRepository.save(libraryAdmin);

        Authentication authentication = new UsernamePasswordAuthenticationToken(libraryAdmin.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_library_administrator")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CreateLibrarianResponseDTO response = authService.createLibrarian(createLibrarianDTO, authentication);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("librarian1", response.getUsername());
        assertEquals("Adam", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertNotNull(response.getTempPassword());
    }

    @Test
    public void whenLibrarianUsernameTakenWithinLibrary_thenThrowException() {
        Address address = new Address();
        address.setStreet("Random");
        address.setCity("Random");
        address.setPostalCode("Random");
        address.setLatitude(BigDecimal.valueOf(10));
        address.setLongitude(BigDecimal.valueOf(10));
        addressRepository.save(address);

        Library library = new Library();
        library.setAddress(address);
        library.setName("Example");
        libraryRepository.save(library);

        Role adminRole = roleRepository.findByName("library_administrator").orElseThrow();
        User libraryAdmin = new User();
        libraryAdmin.setId(userIdGeneratorService.generateUniqueId());
        libraryAdmin.setLibrary(library);
        libraryAdmin.setEmail("library_admin@example.com");
        libraryAdmin.setPassword(passwordEncoder.encode("password"));
        libraryAdmin.setRole(adminRole);
        userRepository.save(libraryAdmin);

        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        User existingLibrarian = new User();
        existingLibrarian.setId(userIdGeneratorService.generateUniqueId());
        existingLibrarian.setUsername("librarian1");
        existingLibrarian.setLibrary(library);
        existingLibrarian.setPassword(passwordEncoder.encode("password"));
        existingLibrarian.setRole(librarianRole);
        userRepository.save(existingLibrarian);

        CreateLibrarianDTO createLibrarianDTO = new CreateLibrarianDTO("librarian1", "Adam", "Smith");

        String authenticationIdentifier = libraryAdmin.getEmail() + ":library_administrator";
        Authentication authentication = new UsernamePasswordAuthenticationToken(authenticationIdentifier, null, List.of(new SimpleGrantedAuthority("ROLE_library_administrator")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createLibrarian(createLibrarianDTO, authentication)
        );

        assertEquals("A librarian with the username '" + createLibrarianDTO.getUsername() + "' already exists in this library", thrownException.getMessage());
    }

    @Test
    public void whenValidLibraryAdminInput_thenReturnCreatedAccountDTO() {
        CreateAdvancedAccountDTO createAdvancedAccountDTO = new CreateAdvancedAccountDTO("library_admin@example.com", "Adam", "Smith", "password");

        CreateAccountResponseDTO response = authService.createAdvancedAccount(createAdvancedAccountDTO, "library_administrator");

        assertNotNull(response);
        assertEquals("library_admin@example.com", response.getEmail());
        assertEquals("Adam", response.getFirstName());
        assertEquals("Smith", response.getLastName());
    }

    @Test
    public void whenLibraryAdminEmailTakenInput_thenThrowException() {
        User existingLibraryAdmin = new User();
        Role libraryAdminRole = roleRepository.findByName("library_administrator").orElseThrow();
        existingLibraryAdmin.setId(userIdGeneratorService.generateUniqueId());
        existingLibraryAdmin.setEmail("library_admin@example.com");
        existingLibraryAdmin.setPassword(passwordEncoder.encode("password"));
        existingLibraryAdmin.setRole(libraryAdminRole);
        userRepository.save(existingLibraryAdmin);

        CreateAdvancedAccountDTO createAdvancedAccountDTO = new CreateAdvancedAccountDTO("library_admin@example.com", "Adam", "Smith", "password");

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createAdvancedAccount(createAdvancedAccountDTO, "library_administrator")
        );

        assertEquals("Email: '" + createAdvancedAccountDTO.getEmail() + "' is already taken for the 'library_administrator' role.", thrownException.getMessage());
    }

    @Test
    public void whenValidDriverInput_thenReturnCreatedAccountDTO() {
        CreateAdvancedAccountDTO createAdvancedAccountDTO = new CreateAdvancedAccountDTO("driver@example.com", "Adam", "Smith", "password");

        CreateAccountResponseDTO response = authService.createAdvancedAccount(createAdvancedAccountDTO, "driver");

        assertNotNull(response);
        assertEquals("driver@example.com", response.getEmail());
        assertEquals("Adam", response.getFirstName());
        assertEquals("Smith", response.getLastName());
    }

    @Test
    public void whenDriverEmailTakenInput_thenThrowException() {
        User existingDriver = new User();
        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        existingDriver.setId(userIdGeneratorService.generateUniqueId());
        existingDriver.setEmail("driver@example.com");
        existingDriver.setPassword(passwordEncoder.encode("password"));
        existingDriver.setRole(driverRole);
        userRepository.save(existingDriver);

        CreateAdvancedAccountDTO createAdvancedAccountDTO = new CreateAdvancedAccountDTO("driver@example.com", "Adam", "Smith", "password");

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createAdvancedAccount(createAdvancedAccountDTO, "driver")
        );

        assertEquals("Email: '" + createAdvancedAccountDTO.getEmail() + "' is already taken for the 'driver' role.", thrownException.getMessage());
    }
}
