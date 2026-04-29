package com.eni.bookhub.service;

import com.eni.bookhub.dto.request.ReservationRequest;
import com.eni.bookhub.dto.response.ReservationResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Reservation;
import com.eni.bookhub.entity.User;
import com.eni.bookhub.mapper.ReservationMapper;
import com.eni.bookhub.repository.BookRepository;
import com.eni.bookhub.repository.ReservationRepository;
import com.eni.bookhub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock BookRepository bookRepository;
    @Mock UserRepository userRepository;
    @Mock LoanService loanService;
    @Mock ReservationMapper reservationMapper;

    @InjectMocks ReservationService reservationService;

    private static final List<Reservation.Status> ACTIVE_STATUSES =
            List.of(Reservation.Status.EN_ATTENTE, Reservation.Status.DISPONIBLE);

    // ── createReservation ──────────────────────────────────────────────────────

    @Test
    void createReservation_whenNoExemplairesDisponibles_throwsBadRequest() {
        stubUserAndBook(0);

        assertThatThrownBy(() -> reservationService.createReservation("user@test.com", request(1)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("aucun exemplaire disponible");
    }

    @Test
    void createReservation_whenAlreadyHasActiveReservation_throwsConflict() {
        stubUserAndBook(2);
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(1, 1, ACTIVE_STATUSES)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation("user@test.com", request(1)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("déjà une réservation active");
    }

    @Test
    void createReservation_whenMaxReservationsReached_throwsConflict() {
        stubUserAndBook(2);
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(1, 1, ACTIVE_STATUSES)).thenReturn(false);
        when(reservationRepository.countByUserIdAndStatusIn(eq(1), anyList())).thenReturn(5);

        assertThatThrownBy(() -> reservationService.createReservation("user@test.com", request(1)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("maximum de 5");
    }

    @Test
    void createReservation_success_createsReservationEnAttente() {
        stubUserAndBook(2);
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(1, 1, ACTIVE_STATUSES)).thenReturn(false);
        when(reservationRepository.countByUserIdAndStatusIn(eq(1), anyList())).thenReturn(0);
        when(reservationRepository.countByBookIdAndStatus(1, Reservation.Status.EN_ATTENTE)).thenReturn(0);
        when(reservationMapper.toResponse(any())).thenReturn(mock(ReservationResponse.class));

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        when(reservationRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        reservationService.createReservation("user@test.com", request(1));

        Reservation saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(Reservation.Status.EN_ATTENTE);
        assertThat(saved.getRankWaitingList()).isEqualTo(1);
    }

    @Test
    void createReservation_rankIsNextInQueue() {
        stubUserAndBook(2);
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(1, 1, ACTIVE_STATUSES)).thenReturn(false);
        when(reservationRepository.countByUserIdAndStatusIn(eq(1), anyList())).thenReturn(1);
        when(reservationRepository.countByBookIdAndStatus(1, Reservation.Status.EN_ATTENTE)).thenReturn(3);
        when(reservationMapper.toResponse(any())).thenReturn(mock(ReservationResponse.class));

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        when(reservationRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        reservationService.createReservation("user@test.com", request(1));

        assertThat(captor.getValue().getRankWaitingList()).isEqualTo(4);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void stubUserAndBook(int exemplaires) {
        User user = User.builder().id(1).email("user@test.com").build();
        Book book = Book.builder().id(1).exemplairesDisponibles(exemplaires).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
    }

    private ReservationRequest request(int bookId) {
        ReservationRequest req = new ReservationRequest();
        req.setBookId(bookId);
        return req;
    }
}
