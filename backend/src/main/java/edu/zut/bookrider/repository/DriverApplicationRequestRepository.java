package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.DriverApplicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DriverApplicationRequestRepository extends JpaRepository<DriverApplicationRequest, Integer> {
    @Query("SELECT COUNT(d) > 0 FROM DriverApplicationRequest d WHERE d.user.id = :userId AND d.status IN (edu.zut.bookrider.model.enums.DriverApplicationStatus.PENDING, edu.zut.bookrider.model.enums.DriverApplicationStatus.UNDER_REVIEW)")
    boolean existsByUserIdAndPendingOrUnderReview(@Param("userId") String userId);
}
