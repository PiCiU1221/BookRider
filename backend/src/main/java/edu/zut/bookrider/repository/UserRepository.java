package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
