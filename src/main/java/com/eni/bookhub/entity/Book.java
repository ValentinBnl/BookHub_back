package com.eni.bookhub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Livres")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @Column(name = "auteur", nullable = false, length = 255)
    private String auteur;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "date_parution", nullable = false)
    private LocalDate dateParution;

    @Column(name = "nombre_pages", nullable = false)
    private Integer nombrePages;

    @Column(name = "description", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "url_couverture", length = 255)
    private String urlCouverture;

    @Column(name = "total_exemplaires", nullable = false)
    private Integer totalExemplaires;

    @Column(name = "exemplaires_disponibles", nullable = false)
    private Integer exemplairesDisponibles;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Category categorie;
}
