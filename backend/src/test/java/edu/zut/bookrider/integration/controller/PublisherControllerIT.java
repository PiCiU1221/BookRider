package edu.zut.bookrider.integration.controller;

import edu.zut.bookrider.model.Publisher;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.PublisherRepository;
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
public class PublisherControllerIT {

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
    private PublisherRepository publisherRepository;

    void createPublisher(String name) {
        Publisher publisher = new Publisher();
        publisher.setName(name);
        publisherRepository.save(publisher);
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
    }

    @Test
    @WithMockUser(username = "example_user@pcit.com", roles = {"user"})
    void whenUserGetsPublishersWithNoInput_thenReturnAllPublishers() throws Exception {

        createPublisher("publisher_pcit1");
        createPublisher("publisher_pcit2");
        createPublisher("publisher_pcit3");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/publishers/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "example_user@pcit.com", roles = {"user"})
    void whenUserGetsPublishersWithInput_thenReturnOnlySelectedPublishers() throws Exception {

        createPublisher("publisher_pcit1");
        createPublisher("publisher_pcit2");
        createPublisher("publisher_pcit3");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/publishers/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "publisher_pcit")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "example_user@pcit.com", roles = {"user"})
    void whenUserGetsPublishersWithInputFromMiddle_thenReturnOnlySelectedPublishers() throws Exception {

        createPublisher("publisher_pcit1");
        createPublisher("publisher_pcit2");
        createPublisher("publisher_pcit3");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/publishers/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", "_pcit")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(3))));
    }
}
