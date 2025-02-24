package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Integer> {
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.language " +
            "LEFT JOIN FETCH b.publisher " +
            "LEFT JOIN FETCH b.authors a " +
            "LEFT JOIN b.libraries l " +
            "WHERE (:libraryId IS NULL OR l.id = :libraryId) " +
            "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
            "AND (:authorName IS NULL OR a.name LIKE %:authorName%) " +
            "AND ((:releaseYearFrom IS NULL OR b.releaseYear >= :releaseYearFrom) " +
            "AND (:releaseYearTo IS NULL OR b.releaseYear <= :releaseYearTo))")
    Page<Book> findAllByFilters(@Param("libraryId") Integer libraryId,
                                @Param("categoryId") Integer categoryId,
                                @Param("authorName") String authorName,
                                @Param("releaseYearFrom") Integer releaseYearFrom,
                                @Param("releaseYearTo") Integer releaseYearTo,
                                Pageable pageable);
}
