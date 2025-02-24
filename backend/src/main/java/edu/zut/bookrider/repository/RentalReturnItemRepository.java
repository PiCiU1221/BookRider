package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Rental;
import edu.zut.bookrider.model.RentalReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalReturnItemRepository extends JpaRepository<RentalReturnItem, Integer> {
    boolean existsByRental(Rental rental);
}
