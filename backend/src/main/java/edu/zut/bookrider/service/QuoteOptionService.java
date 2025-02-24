package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.QuoteOption;
import edu.zut.bookrider.repository.QuoteOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class QuoteOptionService {

    private final QuoteOptionRepository quoteOptionRepository;
    private final DistanceService distanceService;
    private final DeliveryCostCalculatorService deliveryCostCalculatorService;

    public QuoteOption getQuoteOptionById(Integer quoteOptionId) {
        return quoteOptionRepository.findById(quoteOptionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid quote option"));
    }

    public QuoteOption createQuoteOption(
            Library library,
            int quantity,
            double userLat,
            double userLon,
            boolean isLibraryInCart) {

        CoordinateDTO startCoordinates = new CoordinateDTO(userLat, userLon);
        double libraryLat = library.getAddress().getLatitude().doubleValue();
        double libraryLon = library.getAddress().getLongitude().doubleValue();
        CoordinateDTO endCoordinates = new CoordinateDTO(libraryLat, libraryLon);

        BigDecimal distanceKm = distanceService.getDistance(startCoordinates, endCoordinates);
        BigDecimal totalCost = deliveryCostCalculatorService.calculateCost(distanceKm, quantity, isLibraryInCart);

        return QuoteOption.builder()
                .library(library)
                .libraryName(library.getName())
                .distanceKm(distanceKm)
                .totalDeliveryCost(totalCost)
                .build();
    }
}
