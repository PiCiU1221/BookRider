package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.dto.PublisherRequestDto;
import edu.zut.bookrider.dto.PublisherResponseDto;
import edu.zut.bookrider.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public ResponseEntity<?> getAllPublishers() {
        List<PublisherResponseDto> publishers = publisherService.getAllPublishers();
        return ResponseEntity.ok(publishers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPublisherById(@PathVariable Integer id) {
        PublisherResponseDto publisherResponse = publisherService.findPublisherById(id);
        return ResponseEntity.ok(publisherResponse);
    }

    @PreAuthorize("hasRole('librarian')")
    @PostMapping
    public ResponseEntity<?> addPublisher(
            @RequestBody @Valid PublisherRequestDto publisherRequestDto) {

        PublisherResponseDto addedPublisher = publisherService.addPublisher(publisherRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedPublisher);
    }

    @PreAuthorize("hasRole('librarian')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePublisher(
            @PathVariable Integer id,
            @RequestBody @Valid PublisherRequestDto publisherRequestDto) {

        PublisherResponseDto updatedPublisher = publisherService.updatePublisher(id, publisherRequestDto);
        return ResponseEntity.ok(updatedPublisher);
    }

    @PreAuthorize("hasRole('librarian')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePublisher(@PathVariable Integer id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilterResponseDTO>> searchPublishers(@RequestParam("name") String name) {
        Pageable pageable = PageRequest.of(0, 5);

        List<FilterResponseDTO> publishers = publisherService.searchPublishers(name, pageable);

        return ResponseEntity.ok(publishers);
    }
}
