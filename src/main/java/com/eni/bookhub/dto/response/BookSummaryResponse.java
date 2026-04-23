package com.eni.bookhub.dto.response;

import com.eni.bookhub.entity.Book;
import lombok.Getter;

@Getter
public class BookSummaryResponse {

    private final Integer id;
    private final String titre;
    private final String auteur;
    private final String urlCouverture;
    private final Integer totalExemplaires;
    private final Integer exemplairesDisponibles;
    private final String categorie;

    public BookSummaryResponse(Book book) {
        this.id = book.getId();
        this.titre = book.getTitre();
        this.auteur = book.getAuteur();
        this.urlCouverture = book.getUrlCouverture();
        this.totalExemplaires = book.getTotalExemplaires();
        this.exemplairesDisponibles = book.getExemplairesDisponibles();
        this.categorie = book.getCategorie().getNom();
    }
}
