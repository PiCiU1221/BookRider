package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.PublisherRequestDto;
import edu.zut.bookrider.dto.PublisherResponseDto;
import edu.zut.bookrider.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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

    @Secured("librarian")
    @PostMapping
    public ResponseEntity<?> addPublisher(
            @RequestBody @Valid PublisherRequestDto publisherRequestDto) {

        PublisherResponseDto addedPublisher = publisherService.addPublisher(publisherRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedPublisher);
    }

    @Secured("librarian")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePublisher(
            @PathVariable Integer id,
            @RequestBody @Valid PublisherRequestDto publisherRequestDto) {

        PublisherResponseDto updatedPublisher = publisherService.updatePublisher(id, publisherRequestDto);
        return ResponseEntity.ok(updatedPublisher);
    }

    @Secured("librarian")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePublisher(@PathVariable Integer id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}
