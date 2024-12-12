package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.CreateLibraryAdditionDTO;
import edu.zut.bookrider.dto.CreateLibraryAdditionResponseDTO;
import edu.zut.bookrider.service.LibraryAdditionRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library-requests")
public class LibraryAdditionRequestController {

    private final LibraryAdditionRequestService libraryAdditionRequestService;

    @Secured("library_admin")
    @PostMapping
    public ResponseEntity<?> createLibraryRequest(
            @RequestBody @Valid CreateLibraryAdditionDTO createLibraryAdditionDTO,
            Authentication authentication
            ) {
        CreateLibraryAdditionResponseDTO responseDTO = libraryAdditionRequestService.createLibraryRequest(createLibraryAdditionDTO, authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
