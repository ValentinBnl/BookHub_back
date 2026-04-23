package com.eni.bookhub.service;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.repository.BookRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    //  Liste des livres
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::mapToResponse);
    }

    //  Recherche conforme cahier des charges
    public Page<BookResponse> search(String query, String categorie, Boolean disponible, Pageable pageable) {

        Page<Book> page = repository
                .findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                        query, query, query, pageable
                );

        List<Book> filtered = page.getContent();

        // filtre catégorie
        if (categorie != null && !categorie.isBlank()) {
            filtered = filtered.stream()
                    .filter(book -> book.getCategorie() != null &&
                            book.getCategorie().getNom().equalsIgnoreCase(categorie))
                    .collect(Collectors.toList());
        }

        //  filtre disponibilité
        if (disponible != null) {
            filtered = filtered.stream()
                    .filter(book -> disponible
                            ? book.getExemplairesDisponibles() > 0
                            : book.getExemplairesDisponibles() == 0)
                    .collect(Collectors.toList());
        }

        //  reconstruction Page propre
        Page<Book> filteredPage = new PageImpl<>(filtered, pageable, filtered.size());

        return filteredPage.map(this::mapToResponse);
    }

    // 🔹 Mapping
    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .titre(book.getTitre())
                .auteur(book.getAuteur())
                .isbn(book.getIsbn())
                .dateParution(book.getDateParution() != null
                        ? book.getDateParution().toString()
                        : null)
                .nombrePages(book.getNombrePages())
                .description(book.getDescription())
                .urlCouverture(book.getUrlCouverture())
                .totalExemplaires(book.getTotalExemplaires())
                .exemplairesDisponibles(book.getExemplairesDisponibles())
                .categorie(book.getCategorie() != null
                        ? book.getCategorie().getNom()
                        : null)
                .build();
    }
}