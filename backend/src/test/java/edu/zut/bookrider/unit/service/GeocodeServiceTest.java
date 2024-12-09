package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.exception.AddressNotFoundException;
import edu.zut.bookrider.service.GeocodeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GeocodeServiceTest {

    @Autowired
    private GeocodeService geocodeService;

    @Mock
    private RestTemplate restTemplate;

    private static String validLocationsJson;
    private static String noLocationsJson;

    @BeforeAll
    static void setUp() throws IOException {
        validLocationsJson = new String(Files.readAllBytes(Paths.get("src/test/resources/geocodeServiceTest/valid-locations-api-response.json")));
        noLocationsJson = new String(Files.readAllBytes(Paths.get("src/test/resources/geocodeServiceTest/no-locations-api-response.json")));
    }

    @Test
    void whenValidAddress_thenCoordinatesShouldBeCorrect() {
        String street = "Wyszyńskiego 10";
        String city = "Stargard";
        String postalCode = "73-110";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(validLocationsJson);

        CoordinateDTO coordinateDTO = geocodeService.getCoordinatesFromAddress(street, city, postalCode);

        assertNotNull(coordinateDTO);
        assertEquals(15.03, Math.floor(coordinateDTO.getLatitude() * 100) / 100);
        assertEquals(53.33, Math.floor(coordinateDTO.getLongitude() * 100) / 100);
    }

    @Test
    void whenWrongAddress_thenAddressNotFoundExceptionShouldBeThrown() {
        String street = "SomethingRandom 9";
        String city = "SomethingRandom";
        String postalCode = "73-110";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(noLocationsJson);

        AddressNotFoundException thrownException = assertThrows(
                AddressNotFoundException.class,
                () -> geocodeService.getCoordinatesFromAddress(street, city, postalCode)
        );

        assertEquals("No valid address found for the provided coordinates.", thrownException.getMessage());
    }
}
