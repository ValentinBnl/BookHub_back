package com.eni.bookhub.controller;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<Page<BookResponse>> getBooks(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(bookService.getBooks(page));
    }
}
