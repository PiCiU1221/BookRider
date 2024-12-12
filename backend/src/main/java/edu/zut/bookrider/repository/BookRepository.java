package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Integer> {
    @Query("SELECT b FROM Book b " +
           "LEFT JOIN FETCH b.libraries l " +
           "LEFT JOIN FETCH b.authors a " +
           "WHERE (:libraryId IS NULL OR l.id = :libraryId) " +
           "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
           "AND (:authorName IS NULL OR a.name LIKE %:authorName%) " +
           "AND (:releaseYear IS NULL OR b.releaseYear = :releaseYear)")
    Page<Book> findAllByFilters(@Param("libraryId") Integer libraryId,
                                @Param("categoryId") Integer categoryId,
                                @Param("authorName") String authorName,
                                @Param("releaseYear") String releaseYear,
                                Pageable pageable);
}
