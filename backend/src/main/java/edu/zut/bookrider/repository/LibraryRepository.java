package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Library, Integer> {
}
