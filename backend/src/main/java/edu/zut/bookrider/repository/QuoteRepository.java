package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, Integer> {
}
