package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.RentalWithQuantityDTO;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.ShoppingCartItem;
import edu.zut.bookrider.model.ShoppingCartSubItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static edu.zut.bookrider.config.SystemConstants.*;

@Service
@RequiredArgsConstructor
public class DeliveryCostCalculatorService {

    private final DistanceService distanceService;

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

    public BigDecimal calculateTotalItemDeliveryCost(ShoppingCartItem cartItem) {

        Address itemLibraryAddress = cartItem.getLibrary().getAddress();
        double libraryLatitude = itemLibraryAddress.getLatitude().doubleValue();
        double libraryLongitude = itemLibraryAddress.getLongitude().doubleValue();
        CoordinateDTO startCoordinates = new CoordinateDTO(libraryLatitude, libraryLongitude);

        Address targetAddress = cartItem.getShoppingCart().getDeliveryAddress();
        double targetLatitude = targetAddress.getLatitude().doubleValue();
        double targetLongitude = targetAddress.getLongitude().doubleValue();
        CoordinateDTO endCoordinates = new CoordinateDTO(targetLatitude, targetLongitude);

        BigDecimal distance = distanceService.getDistance(startCoordinates, endCoordinates);

        int quantity = 0;
        for (ShoppingCartSubItem subItem : cartItem.getBooks()) {
            quantity += subItem.getQuantity();
        }

        return calculateCost(distance, quantity, false);
    }

    public BigDecimal calculateReturnDeliveryCost(
            List<RentalWithQuantityDTO> rentals,
            Address pickupAddress,
            Library library) {

        double targetLatitude = pickupAddress.getLatitude().doubleValue();
        double targetLongitude = pickupAddress.getLongitude().doubleValue();
        CoordinateDTO startCoordinates = new CoordinateDTO(targetLatitude, targetLongitude);

        Address libraryAddress = library.getAddress();
        double libraryLatitude = libraryAddress.getLatitude().doubleValue();
        double libraryLongitude = libraryAddress.getLongitude().doubleValue();
        CoordinateDTO endCoordinates = new CoordinateDTO(libraryLatitude, libraryLongitude);

        BigDecimal distance = distanceService.getDistance(startCoordinates, endCoordinates);

        int quantity = 0;
        for (RentalWithQuantityDTO rentalDTO : rentals) {
            quantity += rentalDTO.getQuantityToReturn();
        }

        return calculateCost(distance, quantity, false);
    }
}
