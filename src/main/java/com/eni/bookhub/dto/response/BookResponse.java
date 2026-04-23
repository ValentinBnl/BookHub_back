package com.eni.bookhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BookResponse {
    private Integer id;
    private String titre;
    private String auteur;
    private String isbn;
    private LocalDate dateParution;
    private String urlCouverture;
    private Integer totalExemplaires;
    private Integer exemplaireDisponibles;
    private String categorie;
}
