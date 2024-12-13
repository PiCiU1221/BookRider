package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.LibraryAdditionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryAdditionRequestRepository extends JpaRepository<LibraryAdditionRequest, Integer> {
}
