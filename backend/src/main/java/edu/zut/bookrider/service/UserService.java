package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.ChangePasswordDto;
import edu.zut.bookrider.dto.UserProfileDTO;
import edu.zut.bookrider.exception.InsufficientBalanceException;
import edu.zut.bookrider.exception.InvalidPasswordException;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateLibrary(User user, Library library) {

        user.setLibrary(library);
        userRepository.save(user);
    }

    public void verifyUser(User user) {

        user.setIsVerified(true);
        userRepository.save(user);
    }

    public User getUser(Authentication authentication) {

        String identifier = authentication.getName().split(":")[0];
        String role = SecurityUtils.getFirstAuthority();
        role = Objects.requireNonNull(role).substring(5);

        if (isEmail(identifier)) {
            return userRepository.findByEmailAndRoleName(identifier, role)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        } else {
            String[] librarianParts = authentication.getName().split(":");
            String username = librarianParts[0];
            Integer libraryId = Integer.valueOf(librarianParts[1]);
            return userRepository.findByUsernameAndLibraryId(username, libraryId)
                    .orElseThrow(() -> new UserNotFoundException(
                            "User not found with username: " + username + " and library ID: " + libraryId));
        }
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return getUser(authentication);
    }

    private boolean isEmail(String identifier) {
        return identifier.contains("@") && identifier.contains(".");
    }

    public void validateSufficientBalance(User user, BigDecimal requiredAmount) {

        if (user.getBalance().compareTo(requiredAmount) < 0) {
            throw new InsufficientBalanceException("User has insufficient funds for this purchase");
        }
    }

    @Transactional
    public void adjustBalance(User user, BigDecimal amount, boolean isDeposit) {

        if (isDeposit) {
            user.setBalance(user.getBalance().add(amount));
        } else {
            if (user.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
            user.setBalance(user.getBalance().subtract(amount));
        }

        userRepository.save(user);
    }
  
    public User findLibrarianByUsernameAndLibraryId(String username, Integer libraryId) {
        return userRepository.findByUsernameAndLibraryId(username, libraryId)
                .orElseThrow(() -> new UserNotFoundException("Librarian with the provided username " + username + " not found"));
    }

    public List<User> getAllLibrarians(User libraryAdmin) {
        Integer libraryId = libraryAdmin.getLibrary().getId();

        return userRepository.findByRoleNameAndLibraryId("librarian", libraryId);
    }

    public List<User> getAllLibrarians(Integer libraryId) {
        return userRepository.findByRoleNameAndLibraryId("librarian", libraryId);
    }

    public List<User> getAllAdministrators() {
        return userRepository.findByRoleName("system_administrator");
    }

    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto) {
        User user = getUser();

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Old password is not correct");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    public UserProfileDTO getUserProfile() {
        User user = getUser();

        return new UserProfileDTO(
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getLibrary() != null ? user.getLibrary().getId() : null,
                user.getBalance(),
                user.getCreatedAt()
        );
    }
}
