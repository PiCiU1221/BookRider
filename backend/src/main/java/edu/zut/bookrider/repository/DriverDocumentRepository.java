package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Integer> {
}
