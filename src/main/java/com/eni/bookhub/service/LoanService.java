package com.eni.bookhub.service;

import com.eni.bookhub.dto.response.LoanResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Loan;
import com.eni.bookhub.entity.User;
import com.eni.bookhub.repository.BookRepository;
import com.eni.bookhub.repository.LoanRepository;
import com.eni.bookhub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LoanResponse borrowBook(Integer userId, Integer bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre introuvable"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (book.getExemplairesDisponibles() <= 0) {
            throw new RuntimeException("Livre non disponible");
        }

        int activeLoans = loanRepository
                .countByUtilisateurIdAndStatut(userId, "EN COURS");

        if (activeLoans >= 3) {
            throw new RuntimeException("Max 3 emprunts atteints");
        }

        boolean hasLate = loanRepository
                .existsByUtilisateurIdAndStatut(userId, "EN RETARD");

        if (hasLate) {
            throw new RuntimeException("Utilisateur bloqué (retard)");
        }

        // création emprunt (le trigger SQL gère le stock)
        Loan loan = new Loan();
        loan.setUtilisateur(user);
        loan.setLivre(book);
        loan.setDateEmprunt(LocalDateTime.now());
        loan.setDateRetourPrevue(LocalDateTime.now().plusDays(14));
        loan.setStatut("EN COURS");

        loanRepository.save(loan);

        return mapToResponse(loan);
    }

    @Transactional
    public LoanResponse returnBook(Integer loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Emprunt introuvable"));

        if (!loan.getStatut().equals("EN COURS")) {
            throw new RuntimeException("Emprunt déjà retourné");
        }

        LocalDateTime now = LocalDateTime.now();
        loan.setDateRetourEffective(now);

        if (now.isAfter(loan.getDateRetourPrevue())) {
            loan.setStatut("EN RETARD");
        } else {
            loan.setStatut("RENDU");
        }

        // trigger SQL gère le stock
        loanRepository.save(loan);

        return mapToResponse(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getUserLoans(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return loanRepository
                .findByUtilisateurIdAndStatutIn(
                        user.getId(),
                        List.of("EN COURS", "EN RETARD", "RENDU")
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LoanResponse mapToResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .titre(loan.getLivre().getTitre())
                .dateEmprunt(loan.getDateEmprunt().toString())
                .dateRetourPrevue(loan.getDateRetourPrevue().toString())
                .statut(loan.getStatut())
                .build();
    }
}