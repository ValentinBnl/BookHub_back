package com.eni.bookhub.controller;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(bookService.getById(id));
    }
}
