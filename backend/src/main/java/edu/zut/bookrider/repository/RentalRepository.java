package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Rental;
import edu.zut.bookrider.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Integer> {

    Optional<Rental> findByOrderIdAndBookId(Integer orderId, Integer bookId);

    @Query("""
    SELECT r FROM Rental r
    WHERE r.user = :user
    AND r.status != 'RETURNED'
    AND r.status != 'RETURN_IN_PROGRESS'
    ORDER BY 
        CASE r.status
            WHEN 'RENTED' THEN 1
            WHEN 'PARTIALLY_RETURNED' THEN 2
        END,
        r.rentedAt DESC
    """)
    Page<Rental> findAllByUser(@Param("user") User user, Pageable pageable);
}
