package edu.zut.bookrider.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.controller.LibraryAdministratorController;
import edu.zut.bookrider.dto.CreateLibrarianDTO;
import edu.zut.bookrider.dto.CreateLibrarianResponseDTO;
import edu.zut.bookrider.service.LibraryAdministratorService;
import edu.zut.bookrider.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(libraryAdministratorController).build();

        createLibrarianDTO = new CreateLibrarianDTO("test_user", "TestName", "TestLastName");
        librarianResponseDTO = new CreateLibrarianResponseDTO("1", "test_user", "TestName", "TestLastName", "tempPassword");
    }

    @Test
    void getAllLibrarians_shouldReturnAllLibrarians() throws Exception {
        when(libraryAdministratorService.getAllLibrarians("admin@test.com"))
                .thenReturn(List.of(librarianResponseDTO));

        mockMvc.perform(get("/api/library-admins/librarians")
                        .param("libraryAdminEmail", "admin@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("test_user"))
                .andExpect(jsonPath("$[0].firstName").value("TestName"))
                .andExpect(jsonPath("$[0].lastName").value("TestLastName"));

        verify(libraryAdministratorService).getAllLibrarians("admin@test.com");
    }

    @Test
    void addLibrarian_shouldReturnCreatedLibrarian() throws Exception {
        when(authService.createLibrarian(any(CreateLibrarianDTO.class), eq("admin@test.com"))).thenReturn(librarianResponseDTO);

        String jsonBody = objectMapper.writeValueAsString(createLibrarianDTO);

        mockMvc.perform(post("/api/library-admins/librarians")
                        .content(jsonBody)
                        .param("libraryAdminEmail", "admin@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("test_user"))
                .andExpect(jsonPath("$.firstName").value("TestName"))
                .andExpect(jsonPath("$.lastName").value("TestLastName"))
                .andExpect(jsonPath("$.tempPassword").value("tempPassword"));

        verify(authService).createLibrarian(any(CreateLibrarianDTO.class), eq("admin@test.com"));
    }

    @Test
    void removeLibrarian_shouldReturnNoContent() throws Exception {
        doNothing().when(libraryAdministratorService).deleteLibrarian(eq("test_user"), eq("admin@library.com"));

        mockMvc.perform(delete("/api/library-admins/librarians/{username}", "test_user")
                        .param("libraryAdminEmail", "admin@test.com"))
                .andExpect(status().isNoContent());

        verify(libraryAdministratorService).deleteLibrarian(eq("test_user"), eq("admin@test.com"));
    }

    @Test
    void resetLibrarianPassword_shouldReturnUpdatedLibrarian() throws Exception {
        when(libraryAdministratorService.resetLibrarianPassword(eq("test_user"), eq("newPassword"), eq("admin@test.com")))
                .thenReturn(librarianResponseDTO);

        mockMvc.perform(patch("/api/library-admins/librarians/reset-password/{username}", "test_user")
                        .param("newPassword", "newPassword")
                        .param("libraryAdminEmail", "admin@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test_user"))
                .andExpect(jsonPath("$.firstName").value("TestName"))
                .andExpect(jsonPath("$.lastName").value("TestLastName"));

        verify(libraryAdministratorService).resetLibrarianPassword(eq("test_user"), eq("newPassword"), eq("admin@test.com"));
    }
}
