package edu.zut.bookrider.security;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.ShoppingCart;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.RoleRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.UserIdGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserIdGeneratorService userIdGeneratorService;
    private final PasswordEncoder passwordEncoder;

    public String authenticateBasicAccount(LoginRequestDTO loginRequestDTO, String role) throws AuthenticationException {
        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getIdentifier() + ":" + role,
                loginRequestDTO.getPassword()
        );

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationRequest);
            return jwtService.generateToken(authentication.getName());
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }
    }

    public String authenticateLibrarian(LibrarianLoginRequestDTO librarianLoginRequestDTO) throws AuthenticationException {
        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(
                librarianLoginRequestDTO.getUsername() + ":" + librarianLoginRequestDTO.getLibraryId(),
                librarianLoginRequestDTO.getPassword()
        );

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationRequest);
            return jwtService.generateToken(authentication.getName());
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }
    }

    @Transactional
    public CreateAccountResponseDTO createUser(@Valid CreateUserDTO createUserDTO) {

        if (userRepository.existsByEmailAndRoleName(createUserDTO.getEmail(), "user")) {
            throw new IllegalArgumentException("Email: '" + createUserDTO.getEmail() + "' is already taken for the 'user' role.");
        }

        User user = new User();

        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));

        Role userRole = roleRepository.findByName("user")
                .orElseThrow(() -> new IllegalArgumentException("Role not found: user"));
        user.setRole(userRole);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        user.setShoppingCart(shoppingCart);

        User savedUser = userRepository.save(user);

        return new CreateAccountResponseDTO(savedUser);
    }

    public CreateAccountResponseDTO createAdvancedAccount(CreateAdvancedAccountDTO createAdvancedAccountDTO, String roleName) {
        if (userRepository.existsByEmailAndRoleName(createAdvancedAccountDTO.getEmail(), roleName)) {
            throw new IllegalArgumentException("Email: '" + createAdvancedAccountDTO.getEmail() + "' is already taken for the '" + roleName + "' role.");
        }

        User user = new User();
        user.setId(userIdGeneratorService.generateUniqueId());
        user.setEmail(createAdvancedAccountDTO.getEmail());
        user.setFirstName(createAdvancedAccountDTO.getFirstName());
        user.setLastName(createAdvancedAccountDTO.getLastName());
        user.setPassword(passwordEncoder.encode(createAdvancedAccountDTO.getPassword()));

        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        return new CreateAccountResponseDTO(savedUser);
    }

    public CreateLibrarianResponseDTO createLibrarian(
            @Valid CreateLibrarianDTO createLibrarianDTO,
            String libraryAdminEmail) {

        User libraryAdmin = userRepository.findByEmailAndRoleName(libraryAdminEmail, "library_administrator")
                .orElseThrow(() -> new IllegalArgumentException("Library admin with the provided email doesn't exist"));

        Library library = libraryAdmin.getLibrary();

        if (userRepository.existsByUsernameAndLibrary(createLibrarianDTO.getUsername(), library)) {
            throw new IllegalArgumentException("A librarian with the username '" + createLibrarianDTO.getUsername() + "' already exists in this library");
        }

        User librarian = new User();

        librarian.setId(userIdGeneratorService.generateUniqueId());
        librarian.setLibrary(library);
        librarian.setUsername(createLibrarianDTO.getUsername());
        librarian.setFirstName(createLibrarianDTO.getFirstName());
        librarian.setLastName(createLibrarianDTO.getLastName());

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        librarian.setPassword(passwordEncoder.encode(tempPassword));

        Role librarianRole = roleRepository.findByName("librarian")
                .orElseThrow(() -> new IllegalArgumentException("Role not found: librarian"));

        librarian.setRole(librarianRole);

        System.out.println(librarian);

        User savedLibrarian = userRepository.save(librarian);

        return new CreateLibrarianResponseDTO(
                savedLibrarian.getId(),
                savedLibrarian.getUsername(),
                savedLibrarian.getFirstName(),
                savedLibrarian.getLastName(),
                tempPassword
        );
    }
}
