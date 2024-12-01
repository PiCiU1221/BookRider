package edu.zut.bookrider.integration;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.NavigationResponseDTO;
import edu.zut.bookrider.exception.InvalidCoordinatesException;
import edu.zut.bookrider.service.NavigationService;
import edu.zut.bookrider.service.enums.TransportProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class NavigationServiceIntegrationTest {

    @Autowired
    private NavigationService navigationService;

    @Test
    void whenPossibleRouteFromInput_thenNavigationStepsShouldBeCorrect() {
        double startLatitude = 14.504721;
        double startLongitude = 53.434444;

        double endLatitude = 14.506454;
        double endLongitude = 53.433332;

        CoordinateDTO startCoordinates = new CoordinateDTO(startLatitude, startLongitude);
        CoordinateDTO endCoordinates = new CoordinateDTO(endLatitude, endLongitude);

        TransportProfile transportProfile = TransportProfile.CAR;

        NavigationResponseDTO navigationResponseDTO = navigationService.getDirectionsFromCoordinates(startCoordinates, endCoordinates, transportProfile);

        double totalDistance = navigationResponseDTO.getTotalDistance();
        double totalDuration = navigationResponseDTO.getTotalDuration();

        assertEquals(232.1, totalDistance);
        assertEquals(46.4, totalDuration);

        List<NavigationResponseDTO.RouteStep> steps = navigationResponseDTO.getSteps();

        NavigationResponseDTO.RouteStep firstStep = steps.get(0);

        assertEquals(60.4, firstStep.getStepDistance());
        assertEquals(8.7, firstStep.getStepDuration());
        assertEquals("Head east on Krakusa", firstStep.getInstruction());

        List<CoordinateDTO> firstPointWayPoints = firstStep.getWayPoints();
        CoordinateDTO firstPointFirstCoordinate = new CoordinateDTO(14.504724, 53.434459);
        assertEquals(firstPointWayPoints.get(0), firstPointFirstCoordinate);
        CoordinateDTO firstPointSecondCoordinate = new CoordinateDTO(14.50563, 53.4344);
        assertEquals(firstPointWayPoints.get(1), firstPointSecondCoordinate);

        NavigationResponseDTO.RouteStep thirdStep = steps.get(2);

        assertEquals(59.4, thirdStep.getStepDistance());
        assertEquals(10.7, thirdStep.getStepDuration());
        assertEquals("Turn left", thirdStep.getInstruction());

        List<CoordinateDTO> thirdPointWayPoints = thirdStep.getWayPoints();
        CoordinateDTO thirdPointFirstCoordinate = new CoordinateDTO(14.505567, 53.433389);
        assertEquals(thirdPointWayPoints.get(0), thirdPointFirstCoordinate);
        CoordinateDTO thirdPointSecondCoordinate = new CoordinateDTO(14.506455, 53.433336);
        assertEquals(thirdPointWayPoints.get(1), thirdPointSecondCoordinate);
    }

    @Test
    void whenApiResponseHasNoRoute_thenInvalidCoordinatesExceptionShouldBeThrown() {
        CoordinateDTO startCoordinates = new CoordinateDTO(14.504721, 53.434444);
        CoordinateDTO endCoordinates = new CoordinateDTO(14.504721, 53.434444);
        TransportProfile transportProfile = TransportProfile.CAR;

        InvalidCoordinatesException thrownException = assertThrows(
                InvalidCoordinatesException.class,
                () -> navigationService.getDirectionsFromCoordinates(startCoordinates, endCoordinates, transportProfile)
        );

        assertEquals("No valid route found. Please check the coordinates.", thrownException.getMessage());
    }

    @Test
    void whenApiResponseHasInvalidCoordinates_thenInvalidCoordinatesExceptionShouldBeThrown() {
        CoordinateDTO startCoordinates = new CoordinateDTO(14.504721, 53.434444);
        CoordinateDTO endCoordinates = new CoordinateDTO(290.504721, 53.434444);
        TransportProfile transportProfile = TransportProfile.CAR;

        InvalidCoordinatesException thrownException = assertThrows(
                InvalidCoordinatesException.class,
                () -> navigationService.getDirectionsFromCoordinates(startCoordinates, endCoordinates, transportProfile)
        );

        assertEquals("Could not find routable point within a radius of 350.0 meters of specified coordinate 1: 290.5047210 53.4344440.", thrownException.getMessage());
    }
}
