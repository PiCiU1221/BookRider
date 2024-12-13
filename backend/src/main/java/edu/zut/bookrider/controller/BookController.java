package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/filtered")
    public ResponseEntity<?> getFilteredBooks(
            @RequestParam(required = false) Integer libraryId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) int releaseYearFrom,
            @RequestParam(required = false) int releaseYearTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<BookResponseDto> books = bookService.getFilteredBooks(libraryId, categoryId, authorName, releaseYearFrom, releaseYearTo, page, size);
        return ResponseEntity.ok(books);
    }

    @GetMapping
    public ResponseEntity<?> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<BookResponseDto> books = bookService.getAllBooks(page, size);
        return ResponseEntity.ok(books);
    }

    @Secured("librarian")
    @PostMapping
    public ResponseEntity<?> addNewBook(
            @RequestBody @Valid BookRequestDto bookRequestDto,
            Authentication authentication) {

        BookResponseDto addedBook = bookService.addNewBook(bookRequestDto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedBook);
    }

    @Secured("librarian")
    @PostMapping("/add-existing/{bookId}")
    public ResponseEntity<?> addExistingBookToLibrary(
            @PathVariable Integer bookId,
            @RequestParam Integer libraryId) {

        bookService.addExistingBookToLibrary(bookId, libraryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Integer id) {

        BookResponseDto bookResponse = bookService.findBookById(id);
        return ResponseEntity.ok(bookResponse);
    }

    @Secured("librarian")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(
            @PathVariable Integer id,
            @RequestBody @Valid BookRequestDto bookRequestDto) {

        BookResponseDto updatedBook = bookService.updateBook(id, bookRequestDto);
        return ResponseEntity.ok(updatedBook);
    }

    @Secured("librarian")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Integer id) {

        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
