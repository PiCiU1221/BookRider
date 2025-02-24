package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.RentalReturn;
import edu.zut.bookrider.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RentalReturnRepository extends JpaRepository<RentalReturn, Integer> {

    Optional<RentalReturn> findFirstByReturnOrderOrderByReturnedAtDesc(Order returnOrder);

    @Query("""
    SELECT rr FROM RentalReturn rr
    JOIN RentalReturnItem rri ON rri.rentalReturn = rr
    JOIN Rental r ON rri.rental = r
    WHERE r.user = :user
    GROUP BY rr
    ORDER BY rr.status, rr.createdAt DESC
    """)
    Page<RentalReturn> findRentalReturnsByUser(@Param("user") User user, Pageable pageable);
}
