package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Order;
import edu.zut.bookrider.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUserIdAndStatusIn(String userId, List<OrderStatus> statuses);
    List<Order> findByLibraryIdAndStatusIn(Integer libraryId, List<OrderStatus> statuses);
}
