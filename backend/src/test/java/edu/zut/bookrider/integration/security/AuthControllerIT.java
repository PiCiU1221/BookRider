package edu.zut.bookrider.integration.security;

import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.AddressRepository;
import edu.zut.bookrider.repository.LibraryRepository;
import edu.zut.bookrider.repository.RoleRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
public class AuthControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserIdGeneratorService userIdGeneratorService;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    public void whenValidUserCredentials_thenReturnToken() throws Exception {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("test@gmail.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        String role = "user";
        String jsonRequest = "{\"identifier\":\"test@gmail.com\", \"password\":\"password\"}";

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/auth/login/{role}", role)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();
    }

    @Test
    public void whenInvalidUserCredentials_thenReturnErrorResponse() throws Exception {
        String role = "user";
        String jsonRequest = "{\"identifier\":\"test@gmail.com\", \"password\":\"password\"}";

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login/{role}", role)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"code\":401,\"message\":\"Invalid email or password.\"}"))
                .andReturn();
    }

    @Test
    public void whenValidDriverCredentials_thenReturnToken() throws Exception {
        Role driverRole = roleRepository.findByName("driver").orElseThrow();
        User driver = new User();
        driver.setId(userIdGeneratorService.generateUniqueId());
        driver.setEmail("test@gmail.com");
        driver.setRole(driverRole);
        driver.setPassword(passwordEncoder.encode("password"));
        userRepository.save(driver);

        String role = "driver";
        String jsonRequest = "{\"identifier\":\"test@gmail.com\", \"password\":\"password\"}";

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login/{role}", role)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();
    }

    @Test
    public void whenInvalidDriverCredentials_thenReturnErrorResponse() throws Exception {
        String role = "driver";
        String jsonRequest = "{\"identifier\":\"test@gmail.com\", \"password\":\"password\"}";

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login/{role}", role)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"code\":401,\"message\":\"Invalid email or password.\"}"))
                .andReturn();
    }

    @Test
    public void whenValidLibrarianCredentials_thenReturnToken() throws Exception {
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

        String jsonRequest = String.format("{\"username\":\"librarian1\", \"libraryId\":\"%s\", \"password\":\"password\"}", savedLibrary.getId());

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login/librarian")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();
    }

    @Test
    public void whenInvalidLibrarianCredentials_thenReturnErrorResponse() throws Exception {
        String jsonRequest = String.format("{\"username\":\"librarian1\", \"libraryId\":\"%s\", \"password\":\"password\"}", 1);

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login/librarian")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"code\":401,\"message\":\"Invalid email or password.\"}"))
                .andReturn();
    }

    @Test
    public void whenInvalidUserEmailDuringRegister_thenReturnError() throws Exception {
        String role = "user";
        String jsonRequest = "{\"email\":\"test\", \"password\":\"password\"}";

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/register/user", role)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"code\":400,\"message\":\"{email=Email must be a valid email address}\"}"))
                .andReturn();
    }
}
