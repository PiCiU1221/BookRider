package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
