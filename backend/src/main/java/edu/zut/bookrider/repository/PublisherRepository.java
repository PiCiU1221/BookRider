package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    boolean existsByName(String name);
}
