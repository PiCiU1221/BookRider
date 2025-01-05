package edu.zut.bookrider.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.controller.LibraryAdministratorController;
import edu.zut.bookrider.dto.CreateLibrarianDTO;
import edu.zut.bookrider.dto.CreateLibrarianResponseDTO;
import edu.zut.bookrider.security.AuthService;
import edu.zut.bookrider.service.LibraryAdministratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LibraryAdministratorControllerTest {

    @Mock
    private LibraryAdministratorService libraryAdministratorService;

    @Mock
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private LibraryAdministratorController libraryAdministratorController;

    private MockMvc mockMvc;
    private CreateLibrarianDTO createLibrarianDTO;
    private CreateLibrarianResponseDTO librarianResponseDTO;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(libraryAdministratorController).build();

        createLibrarianDTO = new CreateLibrarianDTO("test_user", "TestName", "TestLastName");
        librarianResponseDTO = new CreateLibrarianResponseDTO("1", "test_user", "TestName", "TestLastName", "tempPassword");

        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@test.com");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "library_administrator")
    void getAllLibrarians_shouldReturnAllLibrarians() throws Exception {
        when(libraryAdministratorService.getAllLibrarians(authentication))
                .thenReturn(List.of(librarianResponseDTO));

        mockMvc.perform(get("/api/library-admins/librarians")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("test_user"))
                .andExpect(jsonPath("$[0].firstName").value("TestName"))
                .andExpect(jsonPath("$[0].lastName").value("TestLastName"));

        verify(libraryAdministratorService).getAllLibrarians(authentication);
    }

    @Test
    void addLibrarian_shouldReturnCreatedLibrarian() throws Exception {
        when(authService.createLibrarian(any(CreateLibrarianDTO.class), eq(authentication))).thenReturn(librarianResponseDTO);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        String jsonBody = objectMapper.writeValueAsString(createLibrarianDTO);

        mockMvc.perform(post("/api/library-admins/librarians")
                        .principal(authentication)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("test_user"))
                .andExpect(jsonPath("$.firstName").value("TestName"))
                .andExpect(jsonPath("$.lastName").value("TestLastName"))
                .andExpect(jsonPath("$.tempPassword").value("tempPassword"));

        verify(authService).createLibrarian(any(CreateLibrarianDTO.class), eq(authentication));
    }

    @Test
    void removeLibrarian_shouldReturnNoContent() throws Exception {
        doNothing().when(libraryAdministratorService).deleteLibrarian(eq("test_user"), eq(authentication));

        mockMvc.perform(delete("/api/library-admins/librarians/{username}", "test_user")
                        .principal(authentication))
                .andExpect(status().isNoContent());

        verify(libraryAdministratorService).deleteLibrarian(eq("test_user"), eq(authentication));
    }

    @Test
    void resetLibrarianPassword_shouldReturnUpdatedLibrarian() throws Exception {
        when(libraryAdministratorService.resetLibrarianPassword(eq("test_user"), eq("newPassword"), eq(authentication)))
                .thenReturn(librarianResponseDTO);

        mockMvc.perform(patch("/api/library-admins/librarians/reset-password/{username}", "test_user")
                        .principal(authentication)
                        .param("newPassword", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test_user"))
                .andExpect(jsonPath("$.firstName").value("TestName"))
                .andExpect(jsonPath("$.lastName").value("TestLastName"));

        verify(libraryAdministratorService).resetLibrarianPassword(eq("test_user"), eq("newPassword"), eq(authentication));
    }
}
