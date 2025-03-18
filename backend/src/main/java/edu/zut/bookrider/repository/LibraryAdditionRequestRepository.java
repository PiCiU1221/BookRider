package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.LibraryAdditionRequest;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LibraryAdditionRequestRepository extends JpaRepository<LibraryAdditionRequest, Integer> {
    @Query("SELECT l FROM LibraryAdditionRequest l WHERE (:statuses IS NULL OR l.status IN :statuses)")
    Page<LibraryAdditionRequest> findByStatusIn(@Param("statuses") List<LibraryAdditionStatus> statuses, Pageable pageable);

    @Query("SELECT l FROM LibraryAdditionRequest l WHERE l.createdBy.email = :email")
    Page<LibraryAdditionRequest> findByUser_Email(@Param("email") String email, Pageable pageable);

    @Query("SELECT COUNT(l) > 0 FROM LibraryAdditionRequest l WHERE l.createdBy = :user AND l.status = 'PENDING'")
    boolean existsByCreatedByAndPendingStatus(@Param("user") User user);
}
