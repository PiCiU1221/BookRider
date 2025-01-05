package edu.zut.bookrider.integration.controller;

import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.AddressRepository;
import edu.zut.bookrider.repository.LibraryRepository;
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
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class LibraryAdministratorControllerIT {

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
    private AddressRepository addressRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    private User libraryAdminReference;
    private User librarianReference;

    @BeforeEach
    void setUp() {
        Address address = new Address();
        address.setPostalCode("12312");
        address.setCity("Szczecin");
        address.setStreet("Wyszynskiego 10");
        address.setLatitude(BigDecimal.valueOf(10.0));
        address.setLongitude(BigDecimal.valueOf(10.0));
        Address savedAddress = addressRepository.save(address);

        Library library = new Library();
        library.setAddress(savedAddress);
        library.setName("Library LACIT");
        library.setPhoneNumber("123123123");
        library.setEmail("library_lacit@test.com");
        Library savedLibrary = libraryRepository.save(library);

        Role libraryAdminRole = roleRepository.findByName("library_administrator").orElseThrow();
        User libraryAdmin = new User();
        libraryAdmin.setId(userIdGeneratorService.generateUniqueId());
        libraryAdmin.setEmail("library_administator@gmail.com");
        libraryAdmin.setRole(libraryAdminRole);
        libraryAdmin.setLibrary(savedLibrary);
        libraryAdmin.setIsVerified(true);
        libraryAdmin.setPassword(passwordEncoder.encode("password"));
        libraryAdminReference = userRepository.save(libraryAdmin);

        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        User librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setRole(librarianRole);
        librarian.setLibrary(savedLibrary);
        librarian.setUsername("librarian1");
        librarian.setPassword(passwordEncoder.encode("password"));
        librarian.setFirstName("Adam");
        librarian.setLastName("Smith");
        librarianReference = userRepository.save(librarian);
    }

    @Test
    @WithMockUser(username = "library_administator@gmail.com", roles = {"library_administrator"})
    void whenValidData_thenReturnOkAndLibrarian() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/library-admins/librarians")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("librarian1"))
                .andExpect(jsonPath("$[0].firstName").value("Adam"))
                .andExpect(jsonPath("$[0].lastName").value("Smith"));
    }

    @Test
    @WithMockUser(username = "library_administator@gmail.com", roles = {"library_administrator"})
    void whenValidData_thenReturnOkAndLibrarians() throws Exception {

        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        User librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setRole(librarianRole);
        librarian.setLibrary(libraryAdminReference.getLibrary());
        librarian.setUsername("librarian2");
        librarian.setPassword(passwordEncoder.encode("password"));
        librarian.setFirstName("Michael");
        librarian.setLastName("Smith");
        userRepository.save(librarian);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/library-admins/librarians")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("librarian1"))
                .andExpect(jsonPath("$[0].firstName").value("Adam"))
                .andExpect(jsonPath("$[0].lastName").value("Smith"))
                .andExpect(jsonPath("$[1].username").value("librarian2"))
                .andExpect(jsonPath("$[1].firstName").value("Michael"))
                .andExpect(jsonPath("$[1].lastName").value("Smith"));
    }

    @Test
    @WithMockUser(username = "library_administator@gmail.com", roles = {"library_administrator"})
    void whenValidData_thenReturnOkAndJustOneLibrarian() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/library-admins/librarians")
                        .param("username", librarianReference.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()))
                .andExpect(jsonPath("$.username").value(librarianReference.getUsername()))
                .andExpect(jsonPath("$.firstName").value("Adam"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @WithMockUser(username = "library_administator@gmail.com", roles = {"library_administrator"})
    void whenValidData_thenReturnNoContentAndRemoveLibrarian() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/library-admins/librarians/{username}", librarianReference.getUsername())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Optional<User> deletedUser = userRepository.findById(librarianReference.getId());
        assertFalse(deletedUser.isPresent(), "Librarian should not exist after deletion");
    }

    @Test
    @WithMockUser(username = "library_administator@gmail.com", roles = {"library_administrator"})
    void whenValidData_thenReturnOkAndUpdateLibrarianPassword() throws Exception {

        assertTrue(passwordEncoder.matches("password", librarianReference.getPassword()));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/library-admins/librarians/reset-password/{username}", librarianReference.getUsername())
                        .param("newPassword", "Password1@")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertTrue(passwordEncoder.matches("Password1@", librarianReference.getPassword()));
    }

    @Test
    @WithMockUser(username = "library_administator@gmail.com", roles = {"library_administrator"})
    void whenWrongPasswordFormat_thenReturnBadRequest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/library-admins/librarians/reset-password/{username}", librarianReference.getUsername())
                        .param("newPassword", "password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
