package com.eni.bookhub.repository;

import com.eni.bookhub.entity.Loan;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Integer> {

    //  compter emprunts en cours
    int countByUtilisateurIdAndStatut(Integer utilisateurId, String statut);

    //  vérifier retard
    boolean existsByUtilisateurIdAndStatut(Integer utilisateurId, String statut);

    List<Loan> findByUtilisateurIdAndStatutIn(Integer utilisateurId, List<String> statuts);
}