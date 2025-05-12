package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.DriverApplicationRequest;
import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DriverApplicationRequestRepository extends JpaRepository<DriverApplicationRequest, Integer> {
    @Query("SELECT COUNT(d) > 0 FROM DriverApplicationRequest d WHERE d.user.id = :userId AND d.status IN (edu.zut.bookrider.model.enums.DriverApplicationStatus.PENDING, edu.zut.bookrider.model.enums.DriverApplicationStatus.UNDER_REVIEW)")
    boolean existsByUserIdAndPendingOrUnderReview(@Param("userId") String userId);

    @Query("SELECT d FROM DriverApplicationRequest d WHERE (:statuses IS NULL OR d.status IN :statuses)")
    Page<DriverApplicationRequest> findByStatusIn(@Param("statuses") List<DriverApplicationStatus> statuses, Pageable pageable);

    @Query("SELECT d FROM DriverApplicationRequest d WHERE d.user.email = :email")
    Page<DriverApplicationRequest> findByUserEmail(@Param("email") String email, Pageable pageable);
}
