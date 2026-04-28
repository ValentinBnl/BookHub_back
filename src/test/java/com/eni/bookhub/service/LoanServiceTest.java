package com.eni.bookhub.service;

import com.eni.bookhub.dto.response.LoanResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Loan;
import com.eni.bookhub.entity.User;
import com.eni.bookhub.mapper.LoanMapper;
import com.eni.bookhub.repository.BookRepository;
import com.eni.bookhub.repository.LoanRepository;
import com.eni.bookhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private LoanMapper loanMapper;

    @InjectMocks private LoanService loanService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@test.com")
                .role(User.Role.UTILISATEUR)
                .build();

        book = Book.builder()
                .id(1)
                .titre("Dune")
                .auteur("Frank Herbert")
                .totalExemplaires(5)
                .exemplairesDisponibles(3)
                .build();
    }

    // --- borrowBook ---

    @Test
    void borrowBook_success_createsLoanAndDecrementsAvailability() {
        LoanResponse expected = LoanResponse.builder().id(10).titre("Dune").statut("EN COURS").build();

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(loanRepository.countByUtilisateurIdAndStatut(1, "EN COURS")).thenReturn(0);
        when(loanRepository.existsByUtilisateurIdAndStatut(1, "EN RETARD")).thenReturn(false);
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanMapper.toResponse(any())).thenReturn(expected);

        LoanResponse result = loanService.borrowBook(1, 1);

        assertThat(result.getStatut()).isEqualTo("EN COURS");
        assertThat(book.getExemplairesDisponibles()).isEqualTo(2);
        verify(bookRepository).save(book);
    }

    @Test
    void borrowBook_loanHas14DayReturnWindow() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(loanRepository.countByUtilisateurIdAndStatut(1, "EN COURS")).thenReturn(0);
        when(loanRepository.existsByUtilisateurIdAndStatut(1, "EN RETARD")).thenReturn(false);
        when(loanMapper.toResponse(any())).thenReturn(LoanResponse.builder().build());

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        when(loanRepository.save(loanCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        loanService.borrowBook(1, 1);

        Loan saved = loanCaptor.getValue();
        assertThat(saved.getDateRetourPrevue())
                .isAfterOrEqualTo(saved.getDateEmprunt().plusDays(13))
                .isBeforeOrEqualTo(saved.getDateEmprunt().plusDays(15));
    }

    @Test
    void borrowBook_bookNotFound_throwsException() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrowBook(1, 99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Livre introuvable");
    }

    @Test
    void borrowBook_userNotFound_throwsException() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrowBook(99, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    @Test
    void borrowBook_noAvailableCopies_throwsException() {
        book.setExemplairesDisponibles(0);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loanService.borrowBook(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("non disponible");
    }

    @Test
    void borrowBook_maxActiveLoansReached_throwsException() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(loanRepository.countByUtilisateurIdAndStatut(1, "EN COURS")).thenReturn(3);

        assertThatThrownBy(() -> loanService.borrowBook(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Max 3");
    }

    @Test
    void borrowBook_userHasLateReturn_throwsException() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(loanRepository.countByUtilisateurIdAndStatut(1, "EN COURS")).thenReturn(1);
        when(loanRepository.existsByUtilisateurIdAndStatut(1, "EN RETARD")).thenReturn(true);

        assertThatThrownBy(() -> loanService.borrowBook(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bloqué");
    }

    // --- returnBook ---

    @Test
    void returnBook_onTime_setsStatusRendu() {
        Loan loan = Loan.builder()
                .id(10)
                .livre(book)
                .utilisateur(user)
                .dateEmprunt(LocalDateTime.now().minusDays(5))
                .dateRetourPrevue(LocalDateTime.now().plusDays(9))
                .statut("EN COURS")
                .build();

        when(loanRepository.findById(10)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanMapper.toResponse(any())).thenReturn(LoanResponse.builder().statut("RENDU").build());

        LoanResponse result = loanService.returnBook(10);

        assertThat(loan.getStatut()).isEqualTo("RENDU");
        assertThat(loan.getDateRetourEffective()).isNotNull();
    }

    @Test
    void returnBook_late_setsStatusEnRetard() {
        Loan loan = Loan.builder()
                .id(11)
                .livre(book)
                .utilisateur(user)
                .dateEmprunt(LocalDateTime.now().minusDays(20))
                .dateRetourPrevue(LocalDateTime.now().minusDays(6))
                .statut("EN COURS")
                .build();

        when(loanRepository.findById(11)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanMapper.toResponse(any())).thenReturn(LoanResponse.builder().statut("EN RETARD").build());

        loanService.returnBook(11);

        assertThat(loan.getStatut()).isEqualTo("EN RETARD");
    }

    @Test
    void returnBook_alreadyReturned_throwsException() {
        Loan loan = Loan.builder()
                .id(12)
                .statut("RENDU")
                .build();

        when(loanRepository.findById(12)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnBook(12))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("déjà retourné");
    }

    @Test
    void returnBook_loanNotFound_throwsException() {
        when(loanRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.returnBook(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Emprunt introuvable");
    }

    // --- getUserLoans ---

    @Test
    void getUserLoans_returnsAllStatuses() {
        Loan loanEnCours = Loan.builder().id(1).statut("EN COURS").livre(book).utilisateur(user)
                .dateEmprunt(LocalDateTime.now()).dateRetourPrevue(LocalDateTime.now().plusDays(14)).build();
        Loan loanRendu = Loan.builder().id(2).statut("RENDU").livre(book).utilisateur(user)
                .dateEmprunt(LocalDateTime.now()).dateRetourPrevue(LocalDateTime.now().plusDays(14)).build();

        LoanResponse r1 = LoanResponse.builder().id(1).statut("EN COURS").build();
        LoanResponse r2 = LoanResponse.builder().id(2).statut("RENDU").build();

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(loanRepository.findByUtilisateurIdAndStatutIn(1, List.of("EN COURS", "EN RETARD", "RENDU")))
                .thenReturn(List.of(loanEnCours, loanRendu));
        when(loanMapper.toResponse(loanEnCours)).thenReturn(r1);
        when(loanMapper.toResponse(loanRendu)).thenReturn(r2);

        List<LoanResponse> result = loanService.getUserLoans("jean@test.com");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(LoanResponse::getStatut)
                .containsExactlyInAnyOrder("EN COURS", "RENDU");
    }

    @Test
    void getUserLoans_userNotFound_throwsException() {
        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.getUserLoans("inconnu@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }
}
