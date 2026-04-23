package com.eni.bookhub.mapper;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.dto.response.BookSummaryResponse;
import com.eni.bookhub.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .titre(book.getTitre())
                .auteur(book.getAuteur())
                .isbn(book.getIsbn())
                .dateParution(book.getDateParution())
                .nombrePages(book.getNombrePages())
                .description(book.getDescription())
                .urlCouverture(book.getUrlCouverture())
                .totalExemplaires(book.getTotalExemplaires())
                .exemplairesDisponibles(book.getExemplairesDisponibles())
                .categorie(book.getCategorie().getNom())
                .build();
    }

    public BookSummaryResponse toSummaryResponse(Book book) {
        return BookSummaryResponse.builder()
                .id(book.getId())
                .titre(book.getTitre())
                .auteur(book.getAuteur())
                .urlCouverture(book.getUrlCouverture())
                .totalExemplaires(book.getTotalExemplaires())
                .exemplairesDisponibles(book.getExemplairesDisponibles())
                .categorie(book.getCategorie().getNom())
                .build();
    }
}
