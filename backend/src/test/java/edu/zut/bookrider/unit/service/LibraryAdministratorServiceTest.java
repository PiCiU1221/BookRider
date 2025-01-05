package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.CreateLibrarianResponseDTO;
import edu.zut.bookrider.exception.PasswordNotValidException;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.mapper.user.LibrarianReadMapper;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.LibraryAdministratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.List;
import java.util.Optional;

public class LibraryAdministratorServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LibrarianReadMapper librarianReadMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private LibraryAdministratorService libraryAdministratorService;

    private User libraryAdmin;
    private User librarian;
    private CreateLibrarianResponseDTO librarianResponseDTO;

    @BeforeEach
    void setUp() {
        openMocks(this);

        Library library = new Library();
        library.setId(1);
        library.setName("Test Library");

        libraryAdmin = new User();
        libraryAdmin.setEmail("admin@test.com");
        libraryAdmin.setRole(new Role("library_administrator"));
        libraryAdmin.setLibrary(library);

        librarian = new User();
        librarian.setUsername("Test username");
        librarian.setPassword("password");
        librarian.setRole(new Role("librarian"));
        librarian.setLibrary(library);

        librarianResponseDTO = new CreateLibrarianResponseDTO(librarian.getId(), librarian.getUsername(), librarian.getFirstName(), librarian.getLastName(), "tempPassword");
    }

    @Test
    void deleteLibrarian_shouldDeleteLibrarianSuccessfully() {
        when(userRepository.findByEmailAndRoleName("admin@test.com", "library_administrator")).thenReturn(Optional.of(libraryAdmin));
        when(userRepository.findByUsernameAndLibraryId("Test username", libraryAdmin.getLibrary().getId())).thenReturn(Optional.of(librarian));
        doNothing().when(userRepository).delete(librarian);

        libraryAdministratorService.deleteLibrarian("Test username", "admin@test.com");

        verify(userRepository, times(1)).delete(librarian);
    }

    @Test
    void deleteLibrarian_shouldThrowUserNotFoundException_whenLibrarianNotFound() {
        when(userRepository.findByEmailAndRoleName("admin@test.com", "library_administrator")).thenReturn(Optional.of(libraryAdmin));
        when(userRepository.findByUsernameAndLibraryId("Test username", libraryAdmin.getLibrary().getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> libraryAdministratorService.deleteLibrarian("Test username", "admin@test.com"));
    }

    @Test
    void resetLibrarianPassword_shouldResetPasswordSuccessfully() {
        String newPassword = "NewP@ssw0rd";

        CreateLibrarianResponseDTO librarianResponse = new CreateLibrarianResponseDTO(
                "123", "Test username", "Test FirstName", "Test LastName", "newPassword"
        );

        when(userRepository.findByEmailAndRoleName("admin@test.com", "library_administrator")).thenReturn(Optional.of(libraryAdmin));
        when(userRepository.findByUsernameAndLibraryId("Test username", libraryAdmin.getLibrary().getId())).thenReturn(Optional.of(librarian));
        when(passwordEncoder.encode(newPassword)).thenReturn("newPassword");
        when(userRepository.save(librarian)).thenReturn(librarian);
        when(librarianReadMapper.map(librarian)).thenReturn(librarianResponse);

        CreateLibrarianResponseDTO result = libraryAdministratorService.resetLibrarianPassword("Test username", newPassword, "admin@test.com");

        assertNotNull(result);
        assertEquals("newPassword", result.getTempPassword());
        assertEquals("Test username", result.getUsername());
        assertEquals("Test FirstName", result.getFirstName());
        assertEquals("Test LastName", result.getLastName());
        assertEquals("123", result.getId());
    }

    @Test
    void resetLibrarianPassword_shouldThrowPasswordNotValidException_whenPasswordDoesNotMatchPattern() {
        String newPassword = "invalidPassword";
        when(userRepository.findByEmailAndRoleName("admin@test.com", "library_administrator")).thenReturn(Optional.of(libraryAdmin));
        when(userRepository.findByUsernameAndLibraryId("Test username", libraryAdmin.getLibrary().getId())).thenReturn(Optional.of(librarian));

        assertThrows(PasswordNotValidException.class, () -> libraryAdministratorService.resetLibrarianPassword("Test username", newPassword, "admin@test.com"));
    }

    @Test
    void getAllLibrarians_shouldReturnAllLibrarians() {
        when(userRepository.findByEmailAndRoleName("admin@test.com", "library_administrator")).thenReturn(Optional.of(libraryAdmin));
        when(userRepository.findAll()).thenReturn(List.of(librarian));
        when(librarianReadMapper.map(librarian)).thenReturn(librarianResponseDTO);

        List<CreateLibrarianResponseDTO> result = libraryAdministratorService.getAllLibrarians("admin@test.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test username", result.get(0).getUsername());
    }

    @Test
    void findLibrarianByUsername_shouldReturnLibrarianResponseDTO() {
        when(userRepository.findByEmailAndRoleName("admin@test.com", "library_administrator")).thenReturn(Optional.of(libraryAdmin));
        when(userRepository.findByUsernameAndLibraryId("Test username", libraryAdmin.getLibrary().getId())).thenReturn(Optional.of(librarian));
        when(librarianReadMapper.map(librarian)).thenReturn(librarianResponseDTO);

        CreateLibrarianResponseDTO result = libraryAdministratorService.findLibrarianByUsername("Test username", "admin@test.com");

        assertNotNull(result);
        assertEquals("Test username", result.getUsername());
    }
}
