package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.NavigationResponseDTO;
import edu.zut.bookrider.model.DistanceCache;
import edu.zut.bookrider.repository.DistanceCacheRepository;
import edu.zut.bookrider.service.enums.TransportProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DistanceService {

    private final NavigationService navigationService;
    private final DistanceCacheRepository distanceCacheRepository;

    public BigDecimal getDistance(
            @Valid CoordinateDTO startCoordinates,
            @Valid CoordinateDTO endCoordinates) {

        BigDecimal startLat = BigDecimal.valueOf(startCoordinates.getLatitude());
        BigDecimal startLon = BigDecimal.valueOf(startCoordinates.getLongitude());
        BigDecimal endLat = BigDecimal.valueOf(endCoordinates.getLatitude());
        BigDecimal endLon = BigDecimal.valueOf(endCoordinates.getLongitude());

        Optional<DistanceCache> cachedDistance = distanceCacheRepository.findByCoordinates(startLat, startLon, endLat, endLon);

        if (cachedDistance.isPresent()) {
            return cachedDistance.get().getDistance();
        }

        NavigationResponseDTO responseDTO = navigationService.getDirectionsFromCoordinates(startCoordinates, endCoordinates, TransportProfile.CAR);

        DistanceCache cache = new DistanceCache(
                startLat,
                startLon,
                endLat,
                endLon,
                BigDecimal.valueOf(responseDTO.getTotalDistance())
        );

        DistanceCache savedDistance = distanceCacheRepository.save(cache);

        return savedDistance.getDistance();
    }
}
