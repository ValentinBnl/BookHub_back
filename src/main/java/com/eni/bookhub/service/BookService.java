package com.eni.bookhub.service;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.dto.response.BookSummaryResponse;
import com.eni.bookhub.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> getBooks(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("titre").ascending());
        return bookRepository.findAll(pageable).map(BookSummaryResponse::new);
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Integer id) {
        return bookRepository.findById(id)
                .map(BookResponse::new)
                .orElseThrow(() -> new RuntimeException("Livre introuvable"));
    }
}
