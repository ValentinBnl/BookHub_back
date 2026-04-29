package com.eni.bookhub.service;

import com.eni.bookhub.dto.response.LoanResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Loan;
import com.eni.bookhub.entity.User;
import com.eni.bookhub.mapper.LoanMapper;
import com.eni.bookhub.repository.BookRepository;
import com.eni.bookhub.repository.LoanRepository;
import com.eni.bookhub.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock BookRepository bookRepository;
    @Mock UserRepository userRepository;
    @Mock LoanMapper loanMapper;

    @InjectMocks LoanService loanService;

    // ── returnBook ─────────────────────────────────────────────────────────────

    @Test
    void returnBook_whenEnCours_setsRendu() {
        Loan loan = loanWithStatus("EN COURS");
        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanMapper.toResponse(any())).thenReturn(mock(LoanResponse.class));

        loanService.returnBook(1);

        assertThat(loan.getStatut()).isEqualTo("RENDU");
        assertThat(loan.getDateRetourEffective()).isNotNull();
    }

    @Test
    void returnBook_whenEnRetard_setsRendu() {
        Loan loan = loanWithStatus("EN RETARD");
        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanMapper.toResponse(any())).thenReturn(mock(LoanResponse.class));

        loanService.returnBook(1);

        assertThat(loan.getStatut()).isEqualTo("RENDU");
        assertThat(loan.getDateRetourEffective()).isNotNull();
    }

    @Test
    void returnBook_whenAlreadyRendu_throwsException() {
        Loan loan = loanWithStatus("RENDU");
        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnBook(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("déjà retourné");
    }

    @Test
    void returnBook_whenNotFound_throwsException() {
        when(loanRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.returnBook(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    // ── markOverdueLoans ───────────────────────────────────────────────────────

    @Test
    void markOverdueLoans_marksOverdueLoansAsEnRetard() {
        Loan loan1 = loanWithStatus("EN COURS");
        Loan loan2 = loanWithStatus("EN COURS");
        when(loanRepository.findByStatutAndDateRetourPrevueBefore(eq("EN COURS"), any(LocalDateTime.class)))
                .thenReturn(List.of(loan1, loan2));

        loanService.markOverdueLoans();

        assertThat(loan1.getStatut()).isEqualTo("EN RETARD");
        assertThat(loan2.getStatut()).isEqualTo("EN RETARD");
        verify(loanRepository).saveAll(anyList());
    }

    @Test
    void markOverdueLoans_whenNoneOverdue_savesNothing() {
        when(loanRepository.findByStatutAndDateRetourPrevueBefore(eq("EN COURS"), any(LocalDateTime.class)))
                .thenReturn(List.of());

        loanService.markOverdueLoans();

        verify(loanRepository).saveAll(List.of());
    }

    // ── borrowBook ─────────────────────────────────────────────────────────────

    @Test
    void borrowBook_whenNoExemplaires_throwsException() {
        Book book = Book.builder().id(1).exemplairesDisponibles(0).build();
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> loanService.borrowBook(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("non disponible");
    }

    @Test
    void borrowBook_whenMaxLoansReached_throwsException() {
        Book book = Book.builder().id(1).exemplairesDisponibles(3).build();
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));
        when(loanRepository.countByUtilisateurIdAndStatut(1, "EN COURS")).thenReturn(3);

        assertThatThrownBy(() -> loanService.borrowBook(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Max 3");
    }

    @Test
    void borrowBook_whenHasLateLoans_throwsException() {
        Book book = Book.builder().id(1).exemplairesDisponibles(3).build();
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));
        when(loanRepository.countByUtilisateurIdAndStatut(1, "EN COURS")).thenReturn(0);
        when(loanRepository.existsByUtilisateurIdAndStatut(1, "EN RETARD")).thenReturn(true);

        assertThatThrownBy(() -> loanService.borrowBook(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bloqué");
    }

    @Test
    void borrowBook_success_createsLoan() {
        Book book = Book.builder().id(1).exemplairesDisponibles(3).build();
        User user = User.builder().id(1).build();
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(loanRepository.countByUtilisateurIdAndStatut(1, "EN COURS")).thenReturn(0);
        when(loanRepository.existsByUtilisateurIdAndStatut(1, "EN RETARD")).thenReturn(false);
        when(loanMapper.toResponse(any())).thenReturn(mock(LoanResponse.class));

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        when(loanRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        loanService.borrowBook(1, 1);

        Loan saved = captor.getValue();
        assertThat(saved.getStatut()).isEqualTo("EN COURS");
        assertThat(saved.getDateRetourPrevue()).isAfter(saved.getDateEmprunt());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Loan loanWithStatus(String statut) {
        Loan loan = new Loan();
        loan.setStatut(statut);
        loan.setDateRetourPrevue(LocalDateTime.now().plusDays(14));
        return loan;
    }
}
