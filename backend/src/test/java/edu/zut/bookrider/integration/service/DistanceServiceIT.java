package edu.zut.bookrider.integration.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.service.DistanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
public class DistanceServiceIT {

    @Autowired
    private DistanceService distanceService;

    private CoordinateDTO startCoordinates;
    private CoordinateDTO endCoordinates;

    @BeforeEach
    void setUp() {
        double startLat = 53.430577;
        double startLon = 14.550053;
        startCoordinates = new CoordinateDTO(startLat, startLon);

        double endLat = 53.428178;
        double endLon = 14.539013;
        endCoordinates = new CoordinateDTO(endLat, endLon);
    }

    @Test
    void whenValidData_thenReturnCorrectResult() {
        BigDecimal distance = distanceService.getDistance(startCoordinates, endCoordinates);

        assertEquals(BigDecimal.valueOf(1.1), distance);
    }
}
