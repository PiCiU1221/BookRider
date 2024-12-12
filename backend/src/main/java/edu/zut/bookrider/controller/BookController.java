package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.BookRequestDto;
import edu.zut.bookrider.dto.BookResponseDto;
import edu.zut.bookrider.exception.ApiErrorResponse;
import edu.zut.bookrider.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            @RequestParam(required = false) String releaseYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<BookResponseDto> books = bookService.getFilteredBooks(libraryId, categoryId, authorName, releaseYear, page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("GetFilteredBooksException", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<BookResponseDto> books = bookService.getAllBooks(page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("GetAllBooksException", e.getMessage()));
        }
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addNewBook(
            @RequestPart("bookRequestDto") @Valid BookRequestDto bookRequestDto,
            @RequestParam("image") MultipartFile image,
            @RequestParam("id") Integer id) {

        try {
            BookResponseDto addedBook = bookService.addNewBook(bookRequestDto, image, id);
            return ResponseEntity.ok(addedBook);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("ImageUploadException", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("AddNewBookException", e.getMessage()));
        }
    }

    @PostMapping("/add-existing/{bookId}")
    public ResponseEntity<?> addExistingBookToLibrary(
            @PathVariable Integer bookId,
            @RequestParam Integer libraryId) {

        try {
            bookService.addExistingBookToLibrary(bookId, libraryId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("AddExistingBookException", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Integer id) {
        try {
            BookResponseDto bookResponse = bookService.findBookById(id);
            return ResponseEntity.ok(bookResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("GetBookByIdException", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(
            @PathVariable Integer id,
            @RequestBody @Valid BookRequestDto bookRequestDto) {
        try {
            BookResponseDto updatedBook = bookService.updateBook(id, bookRequestDto);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("UpdateBookException", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Integer id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("DeleteBookException", e.getMessage()));
        }
    }
}
