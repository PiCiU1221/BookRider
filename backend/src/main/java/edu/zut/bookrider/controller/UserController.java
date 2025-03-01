package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.ChangePasswordDto;
import edu.zut.bookrider.dto.IsVerifiedResponseDto;
import edu.zut.bookrider.dto.UserIdResponseDto;
import edu.zut.bookrider.dto.UserProfileDTO;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/id")
    public ResponseEntity<?> getUserId(Authentication authentication) {

        User user = userService.getUser(authentication);
        return ResponseEntity.ok(new UserIdResponseDto(user.getId()));
    }

    @GetMapping("/is-verified")
    public ResponseEntity<?> getUserIsVerified() {

        User user = userService.getUser();

        boolean isVerified = user.getIsVerified();

        return ResponseEntity.ok(new IsVerifiedResponseDto(isVerified));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordDto changePasswordDto) {

        userService.changePassword(changePasswordDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getAccountInfo() {
        UserProfileDTO userProfile = userService.getUserProfile();
        return ResponseEntity.ok(userProfile);
    }
}
