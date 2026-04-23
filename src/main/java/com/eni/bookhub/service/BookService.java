package com.eni.bookhub.service;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse getById(Integer id) {
        return bookRepository.findById(id)
                .map(BookResponse::new)
                .orElseThrow(() -> new RuntimeException("Livre introuvable"));
    }
}
