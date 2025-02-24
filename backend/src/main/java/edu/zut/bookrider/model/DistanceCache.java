package edu.zut.bookrider.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@Table(name = "distance_cache")
public class DistanceCache extends BaseEntity<Integer> {

    @Column(name = "start_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal startLatitude;

    @Column(name = "start_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal startLongitude;

    @Column(name = "end_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal endLatitude;

    @Column(name = "end_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal endLongitude;

    @Column(name = "distance", nullable = false, precision = 9, scale = 2)
    private BigDecimal distance;
}
