package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
