package edu.zut.bookrider.service;

import edu.zut.bookrider.exception.InsufficientBalanceException;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public void validateSufficientBalance(User user, BigDecimal requiredAmount) {

        if (user.getBalance().compareTo(requiredAmount) < 0) {
            throw new InsufficientBalanceException("User has insufficient funds for this purchase");
        }
    }

    @Transactional
    public void adjustBalance(String userId, BigDecimal amount, boolean isDeposit) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
}
