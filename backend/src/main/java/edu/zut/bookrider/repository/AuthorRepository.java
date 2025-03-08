package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Author;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Integer> {
    Optional<Author> findByName(String authorName);
    boolean existsByName(String name);

    @Query("SELECT a FROM Author a WHERE LOWER(a.name) LIKE LOWER(CONCAT(:name, '%'))")
    List<Author> findByNameLike(@Param("name") String name, Pageable pageable);
}
