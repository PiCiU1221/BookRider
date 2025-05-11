package edu.zut.bookrider.repository;

import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmailAndRoleName(String email, String roleName);
    Optional<User> findByEmailAndRoleName(String email, String roleName);
    boolean existsByUsernameAndLibrary(String username, Library library);
    Optional<User> findByUsernameAndLibraryId(String username, Integer id);
    List<User> findByRoleNameAndLibraryId(String roleName, Integer libraryId);
    List<User> findByRoleName(String roleName);
}
