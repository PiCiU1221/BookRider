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
import org.hamcrest.Matchers;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class LibraryControllerIT {

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

    private Address addressReference;

    void createLibrary(String name) {
        Library library = new Library();
        library.setAddress(addressReference);
        library.setName(name);
        libraryRepository.save(library);
    }

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("example_user@lcit.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        Address address = new Address();
        address.setCity("something");
        address.setPostalCode("something");
        address.setStreet("something");
        address.setLatitude(BigDecimal.valueOf(10.0));
        address.setLongitude(BigDecimal.valueOf(10.0));
        addressReference = addressRepository.save(address);
    }

    @Test
    @WithMockUser(username = "example_user@lcit.com:user", roles = {"user"})
    void whenUserGetsLibrariesWithNoInput_thenReturnAllLibraries() throws Exception {

        createLibrary("library_lcit1");
        createLibrary("library_lcit2");
        createLibrary("library_lcit3");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/libraries/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "example_user@lcit.com:user", roles = {"user"})
    void whenUserGetsLibrariesWithInput_thenReturnOnlySelectedLibraries() throws Exception {

        createLibrary("library_lcit1");
        createLibrary("library_lcit2");
        createLibrary("library_lcit3");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/libraries/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "library_lcit")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "example_user@lcit.com:user", roles = {"user"})
    void whenUserGetsLibrariesWithInputFromMiddle_thenReturnOnlySelectedLibraries() throws Exception {

        createLibrary("library_lcit1");
        createLibrary("library_lcit2");
        createLibrary("library_lcit3");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/libraries/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "_lcit")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }
}
