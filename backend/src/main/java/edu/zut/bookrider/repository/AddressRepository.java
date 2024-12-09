package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Integer> {
}
