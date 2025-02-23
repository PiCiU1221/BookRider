package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LibraryRepository extends JpaRepository<Library, Integer> {
    @Query("SELECT COUNT(l) > 0 FROM Library l WHERE l.address.street = :street AND l.address.city = :city AND l.address.postalCode = :postalCode")
    boolean existsByAddress(@Param("street") String street, @Param("city") String city, @Param("postalCode") String postalCode);

    @Query("SELECT COUNT(l) > 0 FROM Library l WHERE l.name = :libraryName AND l.address.city = :city")
    boolean existsByNameAndCity(@Param("libraryName") String libraryName, @Param("city") String city);

    @Query(value = """
        SELECT l.*
        FROM libraries l
        JOIN addresses a ON l.address_id = a.id
        WHERE l.id IN (
            SELECT lb.library_id
            FROM library_books lb
            WHERE lb.book_id = :bookId
        )
        ORDER BY 
            (6371 * acos(
                cos(radians(:userLat)) * cos(radians(a.latitude)) *
                cos(radians(a.longitude) - radians(:userLon)) +
                sin(radians(:userLat)) * sin(radians(a.latitude))
            )) ASC
        LIMIT 5
    """, nativeQuery = true)
    List<Library> findNearestLibrariesWithBook(
            @Param("bookId") Integer bookId,
            @Param("userLat") BigDecimal userLatitude,
            @Param("userLon") BigDecimal userLongitude
    );
}
