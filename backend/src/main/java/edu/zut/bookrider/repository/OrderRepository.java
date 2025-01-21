package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByUserIdAndStatusIn(String userId, List<OrderStatus> statuses, Pageable pageable);
    Page<Order> findByLibraryIdAndStatusIn(Integer libraryId, List<OrderStatus> statuses, Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "JOIN o.library l " +
            "JOIN l.address a " +
            "WHERE o.status = :status " +
            "AND (ST_DistanceSphere(ST_MakePoint(a.latitude, a.longitude), " +
            "ST_MakePoint(:driverLatitude, :driverLongitude)) <= :maxDistanceInMeters)")
    Page<Order> findAcceptedOrdersForDriverWithDistance(
            @Param("status") OrderStatus status,
            @Param("driverLatitude") BigDecimal driverLatitude,
            @Param("driverLongitude") BigDecimal driverLongitude,
            @Param("maxDistanceInMeters") double maxDistanceInMeters,
            Pageable pageable);

    Page<Order> findByDriverIdAndStatusIn(String driverId, List<OrderStatus> status, Pageable pageable);
}
