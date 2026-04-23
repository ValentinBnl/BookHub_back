package com.eni.bookhub.dto.response;

import lombok.AllArgsConstructor;
import com.eni.bookhub.entity.Book;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BookResponse {

    private final Integer id;
    private final String titre;
    private final String auteur;
    private final String isbn;
    private final LocalDate dateParution;
    private final Integer nombrePages;
    private final String description;
    private final String urlCouverture;
    private final Integer totalExemplaires;
    private final Integer exemplairesDisponibles;
    private final String categorie;

    public BookResponse(Book book) {
        this.id = book.getId();
        this.titre = book.getTitre();
        this.auteur = book.getAuteur();
        this.isbn = book.getIsbn();
        this.dateParution = book.getDateParution();
        this.nombrePages = book.getNombrePages();
        this.description = book.getDescription();
        this.urlCouverture = book.getUrlCouverture();
        this.totalExemplaires = book.getTotalExemplaires();
        this.exemplairesDisponibles = book.getExemplairesDisponibles();
        this.categorie = book.getCategorie().getNom();
    }
}
