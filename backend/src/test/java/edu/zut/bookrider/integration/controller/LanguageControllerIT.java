package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.AttributeAddRequestDto;
import edu.zut.bookrider.model.Language;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.LanguageRepository;
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

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class LanguageControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserIdGeneratorService userIdGeneratorService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LanguageRepository languageRepository;
    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Language createLanguage(String languageName) {
        Optional<Language> optionalLanguage = languageRepository.findByName(languageName);

        if (optionalLanguage.isPresent()) {
            return optionalLanguage.get();
        } else {
            Language language = new Language();
            language.setName(languageName);
            return languageRepository.save(language);
        }
    }

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("lcit_user@bookrider.pl");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(userRole);
        userRepository.save(user);

        Library library = libraryRepository.findById(1).orElseThrow();
        Role librarianRole = roleRepository.findByName("librarian").orElseThrow();
        User librarian = new User();
        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setUsername("lcit_librarian");
        librarian.setRole(librarianRole);
        librarian.setLibrary(library);
        librarian.setPassword(passwordEncoder.encode("password"));
        userRepository.save(librarian);
    }

    @Test
    @WithMockUser(username = "lcit_user@bookrider.pl:user", roles = {"user"})
    void whenUserGetsAllLanguages_thenReturnAllLanguages() throws Exception {

        createLanguage("Greek");
        createLanguage("German");
        createLanguage("Korean");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "lcit_librarian:1", roles = {"librarian"})
    void whenLibrarianAddsAuthor_thenReturnCreatedAuthor() throws Exception {

        AttributeAddRequestDto attributeAddRequestDto = new AttributeAddRequestDto("lcit_language");
        String jsonBody = objectMapper.writeValueAsString(attributeAddRequestDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                )
                .andExpect(status().isCreated());
    }
}
