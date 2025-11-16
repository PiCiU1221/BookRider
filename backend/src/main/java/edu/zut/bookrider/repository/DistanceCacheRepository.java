package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.DistanceCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface DistanceCacheRepository extends JpaRepository<DistanceCache, Integer> {
    @Query("SELECT d FROM DistanceCache d WHERE " +
            "(d.startLatitude = :lat1 AND d.startLongitude = :lon1 AND d.endLatitude = :lat2 AND d.endLongitude = :lon2) OR " +
            "(d.startLatitude = :lat2 AND d.startLongitude = :lon2 AND d.endLatitude = :lat1 AND d.endLongitude = :lon1)")
    Optional<DistanceCache> findByCoordinates(
            @Param("lat1") BigDecimal lat1, @Param("lon1") BigDecimal lon1,
            @Param("lat2") BigDecimal lat2, @Param("lon2") BigDecimal lon2);
}
