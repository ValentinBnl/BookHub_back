package com.eni.bookhub.service;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<BookResponse> getAllBooks() {
        return repository.findAll()
                .stream()
                .map(book -> BookResponse.builder()
                        .id(book.getId())
                        .titre(book.getTitre())
                        .auteur(book.getAuteur())
                        .isbn(book.getIsbn())
                        .dateParution(book.getDateParution().toString())
                        .nombrePages(book.getNombrePages())
                        .description(book.getDescription())
                        .urlCouverture(book.getUrlCouverture())
                        .totalExemplaires(book.getTotalExemplaires())
                        .exemplairesDisponibles(book.getExemplairesDisponibles())
                        .categorie(book.getCategorie().getNom())
                        .build())
                .toList();
    }
}