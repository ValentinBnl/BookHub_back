package com.eni.bookhub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Emprunts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livre_id")
    private Book livre;

    @Column(name = "date_emprunt", nullable = false)
    private LocalDateTime dateEmprunt;

    @Column(name = "date_retour_prevue", nullable = false)
    private LocalDateTime dateRetourPrevue;

    @Column(name = "date_retour_effective")
    private LocalDateTime dateRetourEffective;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut;
}
