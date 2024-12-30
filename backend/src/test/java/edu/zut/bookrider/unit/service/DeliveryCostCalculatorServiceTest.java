package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.service.DeliveryCostCalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class DeliveryCostCalculatorServiceTest {

    @Autowired
    private DeliveryCostCalculatorService deliveryCostCalculatorService;

    @Test
    void calculateCostWithLibraryInCart_thenReturnCorrectResult() {

        BigDecimal distanceKm = BigDecimal.valueOf(10.0);
        int totalQuantity = 2;
        boolean isLibraryInCart = true;

        BigDecimal cost = deliveryCostCalculatorService.calculateCost(distanceKm, totalQuantity, isLibraryInCart);

        assertEquals(BigDecimal.valueOf(2.00).setScale(2, RoundingMode.CEILING), cost);
    }

    @Test
    void calculateCostWithoutLibraryInCart_thenReturnCorrectResult() {

        BigDecimal distanceKm = BigDecimal.valueOf(10.0);
        int totalQuantity = 2;
        boolean isLibraryInCart = false;

        BigDecimal cost = deliveryCostCalculatorService.calculateCost(distanceKm, totalQuantity, isLibraryInCart);

        assertEquals(BigDecimal.valueOf(16.00).setScale(2, RoundingMode.CEILING), cost);
    }

    @Test
    void calculateCostWithIncorrectQuantity_thenThrowException() {

        BigDecimal distanceKm = BigDecimal.valueOf(10.0);
        int totalQuantity = 0;
        boolean isLibraryInCart = false;

        assertThrows(IllegalArgumentException.class, () -> {
            deliveryCostCalculatorService.calculateCost(distanceKm, totalQuantity, isLibraryInCart);
        });
    }
}
