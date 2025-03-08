package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.AttributeAddRequestDto;
import edu.zut.bookrider.model.Author;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.AuthorRepository;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class AuthorControllerIT {

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
    private AuthorRepository authorRepository;
    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private ObjectMapper objectMapper;

    void createAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        authorRepository.save(author);
    }

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("example_user@acit.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        Library library = libraryRepository.findById(1).orElseThrow();
        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        User librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setUsername("acit_librarian");
        librarian.setRole(librarianRole);
        librarian.setLibrary(library);
        librarian.setPassword(passwordEncoder.encode("password"));
        userRepository.save(librarian);
    }

    @Test
    @WithMockUser(username = "example_user@acit.com", roles = {"user"})
    void whenUserGetsAuthorsWithNoInput_thenReturnAllAuthors() throws Exception {

        createAuthor("author_acit1");
        createAuthor("author_acit2");
        createAuthor("author_acit3");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "example_user@acit.com", roles = {"user"})
    void whenUserGetsAuthorsWithInput_thenReturnOnlySelectedAuthors() throws Exception {

        createAuthor("author_acit1");
        createAuthor("author_acit2");
        createAuthor("author_acit3");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "author_acit")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "acit_librarian:1", roles = {"librarian"})
    void whenLibrarianAddsAuthor_thenReturnCreatedAuthor() throws Exception {

        AttributeAddRequestDto attributeAddRequestDto = new AttributeAddRequestDto("acit_author");
        String jsonBody = objectMapper.writeValueAsString(attributeAddRequestDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                )
                .andExpect(status().isCreated());
    }
}
