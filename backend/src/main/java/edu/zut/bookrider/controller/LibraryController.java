package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/libraries")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/search")
    public ResponseEntity<List<FilterResponseDTO>> searchLibraries(@RequestParam("name") String name) {
        Pageable pageable = PageRequest.of(0, 5);

        List<FilterResponseDTO> libraries = libraryService.searchLibraries(name, pageable);

        return ResponseEntity.ok(libraries);
    }
}
