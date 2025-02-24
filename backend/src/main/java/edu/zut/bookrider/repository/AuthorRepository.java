package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Integer> {
}
