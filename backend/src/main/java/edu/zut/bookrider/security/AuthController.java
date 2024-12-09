package edu.zut.bookrider.security;

import edu.zut.bookrider.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/{role}")
    public ResponseEntity<?> authenticateAccount(
            @PathVariable String role,
            @Valid @RequestBody LoginRequestDTO loginRequestDTO) {

        String token = authService.authenticateBasicAccount(loginRequestDTO, role);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/login/librarian")
    public ResponseEntity<?> authenticateLibrarian(
            @RequestBody LibrarianLoginRequestDTO librarianLoginRequestDTO) {

        String token = authService.authenticateLibrarian(librarianLoginRequestDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        return ResponseEntity.ok().headers(headers).build();
    }

    @PostMapping("/register/user")
    public ResponseEntity<?> registerUser(@RequestBody @Valid CreateUserDTO createUserDTO) {

        CreateAccountResponseDTO savedUserDTO = authService.createUser(createUserDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserDTO);
    }

    @PostMapping("/register/{role}")
    public ResponseEntity<?> registerAccount(
            @PathVariable String role,
            @RequestBody CreateAdvancedAccountDTO createAdvancedAccountDTO) {

        CreateAccountResponseDTO savedUserDTO = authService.createAdvancedAccount(createAdvancedAccountDTO, role);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserDTO);
    }
}
