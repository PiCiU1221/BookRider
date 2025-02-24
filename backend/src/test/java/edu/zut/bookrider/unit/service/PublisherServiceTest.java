package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.PublisherRequestDto;
import edu.zut.bookrider.dto.PublisherResponseDto;
import edu.zut.bookrider.exception.PublisherNotFoundException;
import edu.zut.bookrider.mapper.publisher.PublisherReadMapper;
import edu.zut.bookrider.model.Publisher;
import edu.zut.bookrider.repository.BookRepository;
import edu.zut.bookrider.repository.PublisherRepository;
import edu.zut.bookrider.service.PublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

public class PublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;
    @Mock
    private PublisherReadMapper publisherReadMapper;
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private PublisherService publisherService;
    private Publisher publisher1;
    private Publisher publisher2;
    private PublisherRequestDto publisherRequestDto;
    private PublisherResponseDto publisherResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        publisher1 = Publisher.builder()
                .name("Test Publisher")
                .build();
        publisher1.setId(1);

        publisher2 = Publisher.builder()
                .name("Test Publisher 2")
                .build();
        publisher2.setId(2);

        publisherRequestDto = new PublisherRequestDto("Test Publisher");
        publisherResponseDto = new PublisherResponseDto(1, "Test Publisher",null);

    }

    @Test
    void addPublisher_shouldAddPublisherSuccessfully() {
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher1);
        when(publisherReadMapper.map(any(Publisher.class))).thenReturn(publisherResponseDto);

        PublisherResponseDto result = publisherService.addPublisher(publisherRequestDto);

        assertNotNull(result);
        assertEquals("Test Publisher", result.getName());
    }

    @Test
    void getAllPublishers_shouldReturnAllPublishers() {
        List<Publisher> publishers = List.of(publisher1, publisher2);
        when(publisherRepository.findAll()).thenReturn(publishers);
        when(publisherReadMapper.map(publisher1)).thenReturn(new PublisherResponseDto(1, "Test Publisher", null));
        when(publisherReadMapper.map(publisher2)).thenReturn(new PublisherResponseDto(2, "Test Publisher 2", null));

        List<PublisherResponseDto> result = publisherService.getAllPublishers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Publisher", result.get(0).getName());
        assertEquals("Test Publisher 2", result.get(1).getName());
    }


    @Test
    void findPublisherById_shouldReturnPublisherResponseDto() {
        when(publisherRepository.findById(anyInt())).thenReturn(Optional.of(publisher1));
        when(publisherReadMapper.map(publisher1)).thenReturn(publisherResponseDto);

        PublisherResponseDto result = publisherService.findPublisherById(1);

        assertNotNull(result);
        assertEquals("Test Publisher", result.getName());
    }

    @Test
    void findPublisherById_shouldThrowPublisherNotFoundException_whenPublisherNotFound() {
        when(publisherRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(PublisherNotFoundException.class, () -> publisherService.findPublisherById(1));
    }

    @Test
    void updatePublisher_shouldUpdatePublisherSuccessfully() {
        when(publisherRepository.findById(anyInt())).thenReturn(Optional.of(publisher1));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher1);
        when(publisherReadMapper.map(any(Publisher.class))).thenReturn(publisherResponseDto);

        PublisherResponseDto result = publisherService.updatePublisher(1, publisherRequestDto);

        assertNotNull(result);
        assertEquals("Test Publisher", result.getName());
        verify(publisherRepository, times(1)).save(any(Publisher.class));
    }

    @Test
    void updatePublisher_shouldThrowPublisherNotFoundException_whenPublisherNotFound() {
        when(publisherRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(PublisherNotFoundException.class, () -> publisherService.updatePublisher(1, publisherRequestDto));
    }

    @Test
    void deletePublisher_shouldDeletePublisherSuccessfully() {
        when(publisherRepository.findById(anyInt())).thenReturn(Optional.of(publisher1));
        doNothing().when(publisherRepository).delete(any(Publisher.class));

        publisherService.deletePublisher(1);

        verify(publisherRepository).delete(publisher1);
    }

    @Test
    void deletePublisher_shouldThrowPublisherNotFoundException_whenPublisherNotFound() {
        when(publisherRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(PublisherNotFoundException.class, () -> publisherService.deletePublisher(1));
    }
}
