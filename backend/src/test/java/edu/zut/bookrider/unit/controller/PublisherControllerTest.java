package edu.zut.bookrider.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.controller.PublisherController;
import edu.zut.bookrider.dto.PublisherRequestDto;
import edu.zut.bookrider.dto.PublisherResponseDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.service.PublisherService;
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
class PublisherControllerTest {

    @Mock
    private PublisherService publisherService;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private PublisherController publisherController;

    private MockMvc mockMvc;
    private PublisherRequestDto publisherRequestDto;
    private PublisherResponseDto publisherResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publisherController).build();

        publisherRequestDto = new PublisherRequestDto("Publisher Name");

        BookResponseDto bookResponseDto = new BookResponseDto(1, "Test Book", "Advantage", List.of("Test Author"), 2008, "Publisher Name", "63643733542", "English", "image");
        publisherResponseDto = new PublisherResponseDto(1, "Publisher Name", List.of(bookResponseDto.getTitle()));
    }

    @Test
    void getAllPublishers_shouldReturnAllPublishers() throws Exception {
        when(publisherService.getAllPublishers()).thenReturn(List.of(publisherResponseDto));

        mockMvc.perform(get("/api/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Publisher Name"))
                .andExpect(jsonPath("$[0].books[0]").value("Test Book"));

        verify(publisherService).getAllPublishers();
    }

    @Test
    void getPublisherById_shouldReturnPublisher() throws Exception {
        when(publisherService.findPublisherById(1)).thenReturn(publisherResponseDto);

        mockMvc.perform(get("/api/publishers/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Publisher Name"))
                .andExpect(jsonPath("$.books[0]").value("Test Book"));

        verify(publisherService).findPublisherById(1);
    }

    @Test
    void addPublisher_shouldReturnAddedPublisherSuccessfully() throws Exception {
        when(publisherService.addPublisher(any(PublisherRequestDto.class))).thenReturn(publisherResponseDto);

        String jsonBody = objectMapper.writeValueAsString(publisherRequestDto);

        mockMvc.perform(post("/api/publishers")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Publisher Name"))
                .andExpect(jsonPath("$.books[0]").value("Test Book"));

        verify(publisherService).addPublisher(any(PublisherRequestDto.class));
    }

    @Test
    void updatePublisher_shouldReturnUpdatedPublisher() throws Exception {
        when(publisherService.updatePublisher(eq(1), any(PublisherRequestDto.class))).thenReturn(publisherResponseDto);

        String jsonBody = objectMapper.writeValueAsString(publisherRequestDto);

        mockMvc.perform(put("/api/publishers/{id}", 1)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Publisher Name"))
                .andExpect(jsonPath("$.books[0]").value("Test Book"));

        verify(publisherService).updatePublisher(eq(1), any(PublisherRequestDto.class));
    }

    @Test
    void deletePublisher_shouldReturnNoContent() throws Exception {
        doNothing().when(publisherService).deletePublisher(1);

        mockMvc.perform(delete("/api/publishers/{id}", 1))
                .andExpect(status().isNoContent());

        verify(publisherService).deletePublisher(1);
    }
}
