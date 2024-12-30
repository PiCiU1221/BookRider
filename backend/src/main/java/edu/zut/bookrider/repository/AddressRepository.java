package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    Optional<Address> findByStreetAndCityAndPostalCode(String street, String city, String postalCode);
}
