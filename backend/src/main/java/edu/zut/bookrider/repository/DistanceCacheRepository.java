package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.DistanceCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface DistanceCacheRepository extends JpaRepository<DistanceCache, Integer> {
    Optional<DistanceCache> findByStartLatitudeAndStartLongitudeAndEndLatitudeAndEndLongitude(
            BigDecimal startLatitude,
            BigDecimal startLongitude,
            BigDecimal endLatitude,
            BigDecimal endLongitude);
}
