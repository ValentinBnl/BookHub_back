package com.eni.bookhub.controller;

import com.eni.bookhub.dto.request.BookRequest;
import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.dto.response.BookSummaryResponse;
import com.eni.bookhub.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<Page<BookSummaryResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "titre") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(bookService.getAllBooks(buildPageable(page, size, sortBy, direction)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookSummaryResponse>> searchBooks(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "titre") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(bookService.search(query, categorie, disponible, buildPageable(page, size, sortBy, direction)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        List<String> allowedFields = List.of("titre", "auteur", "isbn", "dateParution", "nombrePages");
        if (!allowedFields.contains(sortBy)) {
            sortBy = "titre";
        }
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRAIRE', 'ADMIN')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest bookRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(bookRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRAIRE', 'ADMIN')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Integer id, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
