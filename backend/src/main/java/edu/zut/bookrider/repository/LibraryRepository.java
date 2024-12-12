package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibraryRepository extends JpaRepository<Library, Integer> {
    @Query("SELECT COUNT(l) > 0 FROM Library l WHERE l.address.street = :street AND l.address.city = :city AND l.address.postalCode = :postalCode")
    boolean existsByAddress(@Param("street") String street, @Param("city") String city, @Param("postalCode") String postalCode);

    @Query("SELECT COUNT(l) > 0 FROM Library l WHERE l.name = :libraryName AND l.address.city = :city")
    boolean existsByNameAndCity(@Param("libraryName") String libraryName, @Param("city") String city);
}
