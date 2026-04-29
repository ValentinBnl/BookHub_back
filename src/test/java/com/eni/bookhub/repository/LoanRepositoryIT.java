package com.eni.bookhub.repository;

import com.eni.bookhub.AbstractIntegrationTest;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Category;
import com.eni.bookhub.entity.Loan;
import com.eni.bookhub.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoanRepositoryIT extends AbstractIntegrationTest {

    @Autowired private LoanRepository loanRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private CategoryRepository categoryRepository;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.builder().nom("Roman").build());

        user = userRepository.save(User.builder()
                .nom("Dupont").prenom("Jean")
                .email("jean@test.com").telephone("0600000000")
                .motDePasse("$2a$04$hash")
                .role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .build());

        book = bookRepository.save(Book.builder()
                .titre("Dune").auteur("Frank Herbert")
                .isbn("978-0-441-17271-9")
                .dateParution(LocalDate.of(1965, 8, 1))
                .nombrePages(412).description("SF")
                .totalExemplaires(5).exemplairesDisponibles(3)
                .categorie(category)
                .build());
    }

    private Loan saveLoan(String statut, LocalDateTime dateRetourPrevue) {
        return loanRepository.save(Loan.builder()
                .utilisateur(user)
                .livre(book)
                .dateEmprunt(LocalDateTime.now())
                .dateRetourPrevue(dateRetourPrevue)
                .statut(statut)
                .build());
    }

    @Test
    void existsByLivreIdAndStatutIn_activeLoanExists_returnsTrue() {
        saveLoan("EN COURS", LocalDateTime.now().plusDays(14));

        assertThat(loanRepository.existsByLivreIdAndStatutIn(
                book.getId(), List.of("EN COURS", "EN RETARD"))).isTrue();
    }

    @Test
    void existsByLivreIdAndStatutIn_onlyRenduLoans_returnsFalse() {
        saveLoan("RENDU", LocalDateTime.now().plusDays(14));

        assertThat(loanRepository.existsByLivreIdAndStatutIn(
                book.getId(), List.of("EN COURS", "EN RETARD"))).isFalse();
    }

    @Test
    void countByUtilisateurIdAndStatut_returnsCorrectCount() {
        saveLoan("EN COURS", LocalDateTime.now().plusDays(14));
        saveLoan("EN COURS", LocalDateTime.now().plusDays(14));
        saveLoan("RENDU", LocalDateTime.now().plusDays(14));

        int count = loanRepository.countByUtilisateurIdAndStatut(user.getId(), "EN COURS");

        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsByUtilisateurIdAndStatut_lateLoansExist_returnsTrue() {
        saveLoan("EN RETARD", LocalDateTime.now().minusDays(1));

        assertThat(loanRepository.existsByUtilisateurIdAndStatut(user.getId(), "EN RETARD")).isTrue();
    }

    @Test
    void existsByUtilisateurIdAndStatut_noLateLoans_returnsFalse() {
        saveLoan("EN COURS", LocalDateTime.now().plusDays(14));

        assertThat(loanRepository.existsByUtilisateurIdAndStatut(user.getId(), "EN RETARD")).isFalse();
    }

    @Test
    void findByUtilisateurIdAndStatutIn_returnsMatchingLoans() {
        Loan enCours = saveLoan("EN COURS", LocalDateTime.now().plusDays(14));
        Loan enRetard = saveLoan("EN RETARD", LocalDateTime.now().minusDays(1));
        saveLoan("RENDU", LocalDateTime.now().plusDays(7));

        List<Loan> result = loanRepository.findByUtilisateurIdAndStatutIn(
                user.getId(), List.of("EN COURS", "EN RETARD"));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Loan::getStatut)
                .containsExactlyInAnyOrder("EN COURS", "EN RETARD");
    }
}
