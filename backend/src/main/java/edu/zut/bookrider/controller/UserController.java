package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.UserIdResponseDto;
import edu.zut.bookrider.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/id")
    public ResponseEntity<?> getUserId(Authentication authentication) {

        String userId = userService.getUserId(authentication);
        return ResponseEntity.ok(new UserIdResponseDto(userId));
    }
}
