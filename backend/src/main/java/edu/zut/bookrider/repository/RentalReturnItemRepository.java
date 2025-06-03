package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Rental;
import edu.zut.bookrider.model.RentalReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface RentalReturnItemRepository extends JpaRepository<RentalReturnItem, Integer> {
    @Query("""
        SELECT COALESCE(SUM(rri.returnedQuantity), 0)
        FROM RentalReturnItem rri
        WHERE rri.rental.id = :rentalId
    """)
    int sumReturnedQuantityByRentalId(@Param("rentalId") Integer rentalId);

    @Query("""
        SELECT rri.rental.id, SUM(rri.returnedQuantity)
        FROM RentalReturnItem rri
        WHERE rri.rental.id IN :rentalIds
        GROUP BY rri.rental.id
    """)
    List<Object[]> sumReturnedQuantitiesByRentalIds(@Param("rentalIds") List<Integer> rentalIds);
}
