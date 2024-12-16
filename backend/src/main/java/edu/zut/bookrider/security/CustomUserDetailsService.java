package edu.zut.bookrider.security;

import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) {
        User user;

        if (isEmail(identifier)) {
            String[] parts = identifier.split(":");
            if (parts.length == 2) {
                String email = parts[0];
                String roleName = parts[1];
                user = userRepository.findByEmailAndRoleName(email, roleName)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with email: " + email + " and role: " + roleName));
            } else {
                throw new IllegalArgumentException("Invalid identifier format for email-based lookup.");
            }
        } else {
            String[] parts = identifier.split(":");
            if (parts.length == 2) {
                String username = parts[0];
                Integer libraryId = Integer.valueOf(parts[1]);
                user = userRepository.findByUsernameAndLibraryId(username, libraryId)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username: " + username + " in library: " + libraryId));
            } else {
                throw new IllegalArgumentException("Invalid identifier format for username-based lookup.");
            }
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(isEmail(identifier)
                        ? user.getEmail() + ":" + user.getRole().getName()
                        : user.getUsername() + ":" + user.getLibrary().getId())
                .password(user.getPassword())
                .roles(user.getRole().getName())
                .build();
    }

    private boolean isEmail(String identifier) {
        return identifier.contains("@") && identifier.contains(".");
    }
}
