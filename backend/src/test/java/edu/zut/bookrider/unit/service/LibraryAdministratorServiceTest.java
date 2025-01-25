package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.CreateLibrarianResponseDTO;
import edu.zut.bookrider.dto.LibrarianDTO;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.mapper.user.LibrarianReadMapper;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.LibraryAdministratorService;
import edu.zut.bookrider.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class LibraryAdministratorServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LibrarianReadMapper librarianReadMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserService userService;
    @InjectMocks
    private LibraryAdministratorService libraryAdministratorService;

    private User libraryAdmin;
    private User librarian;
    private LibrarianDTO librarianDTO;
    private Authentication authentication;

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

        librarianDTO = new LibrarianDTO("123", "Test username", "Test FirstName", "Test LastName");


        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@test.com");
    }

    @Test
    void deleteLibrarian_shouldDeleteLibrarianSuccessfully() {
        when(userService.getUser(authentication)).thenReturn(libraryAdmin);
        when(userService.findLibrarianByUsernameAndLibraryId("Test username", libraryAdmin.getLibrary().getId())).thenReturn(librarian);
        doNothing().when(userRepository).delete(librarian);

        libraryAdministratorService.deleteLibrarian("Test username", authentication);

        verify(userRepository, times(1)).delete(librarian);
    }

    @Test
    void deleteLibrarian_shouldThrowUserNotFoundException_whenLibrarianNotFound() {
        when(userService.getUser(authentication)).thenReturn(libraryAdmin);
        when(userService.findLibrarianByUsernameAndLibraryId("Test username", libraryAdmin.getLibrary().getId()))
                .thenThrow(new UserNotFoundException("Librarian with the provided username Test username not found"));

        assertThrows(UserNotFoundException.class, () -> libraryAdministratorService.deleteLibrarian("Test username", authentication));
    }

    @Test
    void resetLibrarianPassword_shouldResetPasswordSuccessfully() {
        String username = "Test username";
        String generatedTempPassword = "tempPass";
        String encodedPassword = "encodedTempPass";

        when(userService.getUser()).thenReturn(libraryAdmin);
        when(userService.findLibrarianByUsernameAndLibraryId(username, libraryAdmin.getLibrary().getId()))
                .thenReturn(librarian);
        when(passwordEncoder.encode(generatedTempPassword)).thenReturn(encodedPassword);
        when(userRepository.save(librarian)).thenReturn(librarian);
        CreateLibrarianResponseDTO result = libraryAdministratorService.resetLibrarianPassword(username);

        assertNotNull(result);
        assertEquals(librarian.getId(), result.getId());
        assertEquals(username, result.getUsername());
        assertEquals(librarian.getFirstName(), result.getFirstName());
        assertEquals(librarian.getLastName(), result.getLastName());
        assertNotNull(result.getTempPassword());

        verify(userService).getUser();
        verify(userService).findLibrarianByUsernameAndLibraryId(username, libraryAdmin.getLibrary().getId());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(librarian);
    }

    @Test
    void getAllLibrarians_shouldReturnAllLibrarians() {
        when(userService.getUser(authentication)).thenReturn(libraryAdmin);
        when(userService.getAllLibrarians(libraryAdmin)).thenReturn(List.of(librarian));
        when(librarianReadMapper.map(librarian)).thenReturn(librarianDTO);

        List<LibrarianDTO> result = libraryAdministratorService.getAllLibrarians(authentication);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test username", result.get(0).getUsername());
    }

    @Test
    void findLibrarianByUsername_shouldReturnLibrarianResponseDTO() {
        when(userService.getUser(authentication)).thenReturn(libraryAdmin);
        when(userService.findLibrarianByUsernameAndLibraryId("Test username", libraryAdmin.getLibrary().getId())).thenReturn(librarian);
        when(librarianReadMapper.map(librarian)).thenReturn(librarianDTO);

        LibrarianDTO result = libraryAdministratorService.findLibrarianByUsername("Test username", authentication);

        assertNotNull(result);
        assertEquals("Test username", result.getUsername());
    }
}
