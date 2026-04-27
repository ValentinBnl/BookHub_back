package com.eni.bookhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookRequest {
    @NotBlank
    private String titre;
    @NotBlank
    private String isbn;
    @NotBlank
    private String auteur;
    @NotNull
    private LocalDate dateParution;
    @NotNull
    private Integer nombrePages;
    @NotBlank
    private String description;
    private String urlCouverture;
    @NotNull
    private Integer totalExemplaires;
    @NotNull
    private Integer categorieId;
}
