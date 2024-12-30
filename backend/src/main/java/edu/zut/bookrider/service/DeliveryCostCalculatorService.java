package edu.zut.bookrider.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DeliveryCostCalculatorService {

    private static final BigDecimal BASE_COST = BigDecimal.valueOf(10.00);
    private static final BigDecimal PER_KM_RATE = BigDecimal.valueOf(0.50);
    private static final BigDecimal ADDITIONAL_ITEM_COST = BigDecimal.valueOf(1.00);

    public BigDecimal calculateCost(BigDecimal distanceKm, int totalQuantity, boolean isLibraryInCart) {
        if (totalQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        BigDecimal cost;
        if (isLibraryInCart) {
            cost = ADDITIONAL_ITEM_COST.multiply(BigDecimal.valueOf(totalQuantity));
        } else {
            BigDecimal baseDeliveryCost = BASE_COST.add(distanceKm.multiply(PER_KM_RATE));
            int additionalItems = Math.max(totalQuantity - 1, 0);
            BigDecimal additionalCost = ADDITIONAL_ITEM_COST.multiply(BigDecimal.valueOf(additionalItems));
            cost = baseDeliveryCost.add(additionalCost);
        }

        return cost.setScale(2, RoundingMode.CEILING);
    }
}
