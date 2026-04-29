package com.eni.bookhub.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookSummaryResponse {
    private Integer id;
    private String titre;
    private String auteur;
    private String urlCouverture;
    private Integer totalExemplaires;
    private Integer exemplairesDisponibles;
    private String categorie;
    private LocalDate dateParution;
}
