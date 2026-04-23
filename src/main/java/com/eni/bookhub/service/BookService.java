package com.eni.bookhub.service;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.dto.response.BookSummaryResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.repository.BookRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookSummaryResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> search(String query, String categorie, Boolean disponible, Pageable pageable) {
        Page<Book> page = bookRepository
                .findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                        query, query, query, pageable
                );

        List<Book> filtered = page.getContent();

        if (categorie != null && !categorie.isBlank()) {
            filtered = filtered.stream()
                    .filter(book -> book.getCategorie() != null &&
                            book.getCategorie().getNom().equalsIgnoreCase(categorie))
                    .collect(Collectors.toList());
        }

        if (disponible != null) {
            filtered = filtered.stream()
                    .filter(book -> disponible
                            ? book.getExemplairesDisponibles() > 0
                            : book.getExemplairesDisponibles() == 0)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(filtered, pageable, filtered.size()).map(BookSummaryResponse::new);
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Integer id) {
        return bookRepository.findById(id)
                .map(BookResponse::new)
                .orElseThrow(() -> new RuntimeException("Livre introuvable"));
    }
}
