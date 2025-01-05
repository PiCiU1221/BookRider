package edu.zut.bookrider.service;

import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public void updateLibrary(User user, Library library) {

        user.setLibrary(library);
        userRepository.save(user);
    }

    public void verifyUser(User user) {

        user.setIsVerified(true);
        userRepository.save(user);
    }

    public User getUser(Authentication authentication) {

        String userEmail = authentication.getName().split(":")[0];
        String role = SecurityUtils.getFirstAuthority();
        role = Objects.requireNonNull(role).substring(5);

        return userRepository.findByEmailAndRoleName(userEmail, role)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User findLibrarianByUsernameAndLibraryId(String username, Integer libraryId) {
        return userRepository.findByUsernameAndLibraryId(username, libraryId)
                .orElseThrow(() -> new UserNotFoundException("Librarian with the provided username " + username + " not found"));
    }

    public List<User> getAllLibrarians(User libraryAdmin) {
        Integer libraryId = libraryAdmin.getLibrary().getId();

        return userRepository.findAll().stream()
                .filter(user -> "librarian".equals(user.getRole().getName()) && user.getLibrary().getId().equals(libraryId))
                .toList();
    }
}
