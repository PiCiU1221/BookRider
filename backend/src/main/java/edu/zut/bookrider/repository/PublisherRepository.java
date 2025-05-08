package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Publisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    boolean existsByName(String name);
    Optional<Publisher> findByName(String name);

    @Query("SELECT p FROM Publisher p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Publisher> findByNameLike(@Param("name") String name, Pageable pageable);
}
