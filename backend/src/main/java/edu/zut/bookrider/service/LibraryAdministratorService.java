package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CreateLibrarianResponseDTO;
import edu.zut.bookrider.exception.PasswordNotValidException;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.mapper.user.LibrarianReadMapper;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public void deleteLibrarian(String username, String libraryAdminEmail) {
        User libraryAdmin = findLibraryAdminByEmail(libraryAdminEmail);
        Integer libraryId = libraryAdmin.getLibrary().getId();

        User librarian = findLibrarianByUsernameAndLibraryId(username, libraryId);

        userRepository.delete(librarian);
    }

    @Transactional
    public CreateLibrarianResponseDTO resetLibrarianPassword(String username, String newPassword, String libraryAdminEmail) {
        User libraryAdmin = findLibraryAdminByEmail(libraryAdminEmail);
        Integer libraryId = libraryAdmin.getLibrary().getId();

        User librarian = findLibrarianByUsernameAndLibraryId(username, libraryId);

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

    public List<CreateLibrarianResponseDTO> getAllLibrarians(String libraryAdminEmail) {
        User libraryAdmin = findLibraryAdminByEmail(libraryAdminEmail);
        Integer libraryId = libraryAdmin.getLibrary().getId();

        return userRepository.findAll().stream()
                .filter(user -> "librarian".equals(user.getRole().getName()) && user.getLibrary().getId().equals(libraryId))
                .map(librarianReadMapper::map)
                .toList();
    }

    public CreateLibrarianResponseDTO findLibrarianByUsername(String username, String libraryAdminEmail) {
        User libraryAdmin = findLibraryAdminByEmail(libraryAdminEmail);
        Integer libraryId = libraryAdmin.getLibrary().getId();

        User librarian = findLibrarianByUsernameAndLibraryId(username, libraryId);
        return librarianReadMapper.map(librarian);
    }

    private User findLibraryAdminByEmail(String libraryAdminEmail) {
        return userRepository.findByEmailAndRoleName(libraryAdminEmail, "library_administrator")
                .orElseThrow(() -> new UserNotFoundException("Library admin with the provided email doesn't exist"));
    }

    private User findLibrarianByUsernameAndLibraryId(String username, Integer libraryId) {
        return userRepository.findByUsernameAndLibraryId(username, libraryId)
                .orElseThrow(() -> new UserNotFoundException("Librarian with the provided username " + username + " not found"));
    }
}
