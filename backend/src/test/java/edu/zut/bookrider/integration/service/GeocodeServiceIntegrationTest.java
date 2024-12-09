package edu.zut.bookrider.integration.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.exception.AddressNotFoundException;
import edu.zut.bookrider.service.GeocodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GeocodeServiceIntegrationTest {

    @Autowired
    private GeocodeService geocodeService;

    @Test
    void whenValidAddress_thenCoordinatesShouldBeCorrect() {
        String street = "Wyszyńskiego 10";
        String city = "Stargard";
        String postalCode = "73-110";

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

        AddressNotFoundException thrownException = assertThrows(
                AddressNotFoundException.class,
                () -> geocodeService.getCoordinatesFromAddress(street, city, postalCode)
        );

        assertEquals("No valid address found for the provided coordinates.", thrownException.getMessage());
    }
}
