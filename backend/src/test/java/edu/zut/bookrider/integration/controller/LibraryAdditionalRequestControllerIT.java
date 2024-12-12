package edu.zut.bookrider.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CreateLibraryAdditionDTO;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class LibraryAdditionalRequestControllerIT {

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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName("library_administrator").orElseThrow();
        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail("library_administator@gmail.com");
        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "library_administator@gmail.com", roles = {"library_administrator"})
    void whenValidInput_returnDTO() throws Exception {
        CreateLibraryAdditionDTO createLibraryAdditionDTO = new CreateLibraryAdditionDTO();
        createLibraryAdditionDTO.setStreet("Wojska Polskiego 14");
        createLibraryAdditionDTO.setCity("Szczecin");
        createLibraryAdditionDTO.setPostalCode("73123");
        createLibraryAdditionDTO.setLibraryName("Filia nr. 5");
        createLibraryAdditionDTO.setPhoneNumber("123123123");
        createLibraryAdditionDTO.setLibraryEmail("filia5@szczecin.gov.pl");

        String jsonBody = objectMapper.writeValueAsString(createLibraryAdditionDTO);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/library-requests")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        //System.out.println("Response Body: " + responseBody);
    }
}
