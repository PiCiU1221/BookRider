package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.LibrarianDTO;
import edu.zut.bookrider.exception.PasswordNotValidException;
import edu.zut.bookrider.mapper.user.LibrarianReadMapper;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LibraryAdministratorService {

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LibrarianReadMapper librarianReadMapper;
    private final UserService userService;

    @Transactional
    public void deleteLibrarian(String username, Authentication authentication) {
        User libraryAdmin = userService.getUser(authentication);
        Integer libraryId = libraryAdmin.getLibrary().getId();

        User librarian = userService.findLibrarianByUsernameAndLibraryId(username, libraryId);

        userRepository.delete(librarian);
    }

    @Transactional
    public LibrarianDTO resetLibrarianPassword(String username, String newPassword, Authentication authentication) {
        User libraryAdmin = userService.getUser(authentication);
        Integer libraryId = libraryAdmin.getLibrary().getId();

        User librarian = userService.findLibrarianByUsernameAndLibraryId(username, libraryId);

        if (newPassword.matches(PASSWORD_PATTERN)) {
            librarian.setPassword(passwordEncoder.encode(newPassword));
        } else {
            throw new PasswordNotValidException(
                    "Password must contain at least one uppercase letter, " +
                    "one lowercase letter, one digit, and one special character : @$!%*?&. " +
                    "It must be at least 8 characters long."
            );
        }

        userRepository.save(librarian);

        return librarianReadMapper.map(librarian);
    }

    public List<LibrarianDTO> getAllLibrarians(Authentication authentication) {
        User libraryAdmin = userService.getUser(authentication);
        List<User> librarians = userService.getAllLibrarians(libraryAdmin);

        return librarians.stream()
                .map(librarianReadMapper::map)
                .toList();
    }

    public LibrarianDTO findLibrarianByUsername(String username, Authentication authentication) {
        User libraryAdmin = userService.getUser(authentication);
        Integer libraryId = libraryAdmin.getLibrary().getId();

        User librarian = userService.findLibrarianByUsernameAndLibraryId(username, libraryId);
        return librarianReadMapper.map(librarian);
    }
}
