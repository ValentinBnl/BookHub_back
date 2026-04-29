package com.eni.bookhub.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BookResponse {
    private Integer id;
    private String titre;
    private String auteur;
    private String isbn;
    private LocalDate dateParution;
    private Integer nombrePages;
    private String description;
    private String urlCouverture;
    private Integer totalExemplaires;
    private Integer exemplairesDisponibles;
    private String categorie;
    private Integer categorieId;
}
