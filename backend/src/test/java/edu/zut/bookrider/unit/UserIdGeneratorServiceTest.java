package edu.zut.bookrider.unit;

import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.UserIdGeneratorService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserIdGeneratorServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserIdGeneratorService userIdGeneratorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenNoConflicts_thenReturnFirstGeneratedId() {
        when(userRepository.existsById(anyString())).thenReturn(false);

        String uniqueId = userIdGeneratorService.generateUniqueId();

        assertThat(uniqueId).hasSize(10).matches("^[A-Z0-9]+$");
        verify(userRepository, times(1)).existsById(anyString());
    }

    @Test
    void whenConflicts_thenReturnNotFirstGeneratedId() {
        when(userRepository.existsById(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        String uniqueId = userIdGeneratorService.generateUniqueId();

        assertThat(uniqueId).hasSize(10).matches("^[A-Z0-9]+$");
        verify(userRepository, times(2)).existsById(anyString());
    }
}
