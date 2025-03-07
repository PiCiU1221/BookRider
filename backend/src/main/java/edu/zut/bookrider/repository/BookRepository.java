package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Book;
import edu.zut.bookrider.model.Library;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {
    @Query("""
    SELECT DISTINCT b
    FROM Book b
    LEFT JOIN FETCH b.category
    LEFT JOIN FETCH b.language
    LEFT JOIN FETCH b.publisher
    LEFT JOIN FETCH b.authors a
    LEFT JOIN b.libraries l
    WHERE (COALESCE(:title, '') = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:library IS NULL OR l.name = :library)
      AND (:category IS NULL OR b.category.name = :category)
      AND (:language IS NULL OR b.language.name = :language)
      AND (:publisher IS NULL OR b.publisher.name = :publisher)
      AND ((:releaseYearFrom IS NULL OR b.releaseYear >= :releaseYearFrom)
      AND (:releaseYearTo IS NULL OR b.releaseYear <= :releaseYearTo))
      AND (
          :authorNames IS NULL OR (
              (SELECT COUNT(DISTINCT a2.name)
               FROM Book b2
               JOIN b2.authors a2
               WHERE b2.id = b.id
                 AND a2.name IN :authorNames
              ) = :#{#authorNames == null ? 0 : #authorNames.size()})
      )
    """)
    Page<Book> findAllByFilters(
            @Param("title") String title,
            @Param("library") String library,
            @Param("category") String category,
            @Param("language") String language,
            @Param("publisher") String publisher,
            @Param("authorNames") List<String> authorNames,
            @Param("releaseYearFrom") Integer releaseYearFrom,
            @Param("releaseYearTo") Integer releaseYearTo,
            Pageable pageable
    );

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Book> findByTitleLike(@Param("title") String title, Pageable pageable);
}
