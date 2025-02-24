package edu.zut.bookrider.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@Table(name = "quote_options")
public class QuoteOption extends BaseEntity<Integer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Column(name = "distance_km", nullable = false)
    private BigDecimal distanceKm;

    @Column(name = "total_delivery_cost", nullable = false)
    private BigDecimal totalDeliveryCost;

    @Column(name = "library_name", nullable = false)
    private String libraryName;
}
