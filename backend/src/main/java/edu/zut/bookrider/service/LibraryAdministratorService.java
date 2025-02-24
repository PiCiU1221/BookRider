package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CreateLibrarianResponseDTO;
import edu.zut.bookrider.dto.LibrarianDTO;
import edu.zut.bookrider.mapper.user.LibrarianReadMapper;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LibraryAdministratorService {

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
    public CreateLibrarianResponseDTO resetLibrarianPassword(String username) {
        User libraryAdmin = userService.getUser();
        Integer libraryId = libraryAdmin.getLibrary().getId();

        User librarian = userService.findLibrarianByUsernameAndLibraryId(username, libraryId);

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        librarian.setPassword(passwordEncoder.encode(tempPassword));
        User savedLibrarian = userRepository.save(librarian);

        return new CreateLibrarianResponseDTO(
                savedLibrarian.getId(),
                savedLibrarian.getUsername(),
                savedLibrarian.getFirstName(),
                savedLibrarian.getLastName(),
                tempPassword
        );
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
