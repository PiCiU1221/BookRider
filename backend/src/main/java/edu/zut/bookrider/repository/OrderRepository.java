package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByUserIdAndStatusIn(String userId, List<OrderStatus> statuses, Pageable pageable);
    Page<Order> findByLibraryIdAndStatusIn(Integer libraryId, List<OrderStatus> statuses, Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "JOIN o.library l " +
            "JOIN l.address a " +
            "WHERE " +
            "((o.status = 'ACCEPTED' AND o.isReturn = false) " +
            "OR (o.status = 'PENDING' AND o.isReturn = true)) " +
            "AND (ST_DistanceSphere(ST_MakePoint(a.latitude, a.longitude), " +
            "ST_MakePoint(:driverLatitude, :driverLongitude)) <= :maxDistanceInMeters) " +

            "ORDER BY " +
            "CASE WHEN (" +
            "   (o.status = 'ACCEPTED' AND o.isReturn = false AND o.acceptedAt < :olderThanDate) " +
            "   OR " +
            "   (o.status = 'PENDING' AND o.isReturn = true AND o.createdAt < :olderThanDate) " +
            ") THEN 0 ELSE 1 END ASC, " +

            "ST_DistanceSphere(ST_MakePoint(a.latitude, a.longitude), " +
            "ST_MakePoint(:driverLatitude, :driverLongitude)) ASC")
    Page<Order> findOrdersForDriverWithDistance(
            @Param("driverLatitude") BigDecimal driverLatitude,
            @Param("driverLongitude") BigDecimal driverLongitude,
            @Param("maxDistanceInMeters") double maxDistanceInMeters,
            @Param("olderThanDate") LocalDateTime olderThanDate,
            Pageable pageable);

    Page<Order> findByDriverIdAndStatusIn(String driverId, List<OrderStatus> status, Pageable pageable);

    boolean existsByDriverAndStatusNot(User driver, OrderStatus status);

    Optional<Order> findFirstByDriverIdAndStatusIn(String driverId, List<OrderStatus> statuses);
}
