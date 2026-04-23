package com.eni.bookhub.controller;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<BookResponse> getAllBooks() {
        return service.getAllBooks();
    }
}