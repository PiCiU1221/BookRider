package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.LibraryCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibraryCardRepository extends JpaRepository<LibraryCard, Integer> {
    boolean existsByCardId(String cardId);

    @Query("SELECT lc FROM LibraryCard lc WHERE lc.user.id = :userId")
    Page<LibraryCard> findByUserId(@Param("userId") String userId, Pageable pageable);
}
