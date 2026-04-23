package com.eni.bookhub.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Integer id;
    private String titre;
    private String auteur;
    private String isbn;
    private String dateParution;
    private Integer nombrePages;
    private String description;
    private String urlCouverture;
    private Integer totalExemplaires;
    private Integer exemplairesDisponibles;
    private String categorie;
}