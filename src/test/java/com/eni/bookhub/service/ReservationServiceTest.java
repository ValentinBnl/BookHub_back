package com.eni.bookhub.service;

import com.eni.bookhub.dto.request.ReservationRequest;
import com.eni.bookhub.dto.response.LoanResponse;
import com.eni.bookhub.dto.response.ReservationResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Reservation;
import com.eni.bookhub.entity.User;
import com.eni.bookhub.mapper.ReservationMapper;
import com.eni.bookhub.repository.BookRepository;
import com.eni.bookhub.repository.ReservationRepository;
import com.eni.bookhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.eni.bookhub.entity.Reservation.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private LoanService loanService;
    @Mock private ReservationMapper reservationMapper;

    @InjectMocks private ReservationService reservationService;

    private static final List<Reservation.Status> ACTIVE = List.of(EN_ATTENTE, DISPONIBLE);

    private User user;
    private Book book;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1).nom("Dupont").prenom("Jean")
                .email("jean@test.com")
                .role(User.Role.UTILISATEUR)
                .build();

        book = Book.builder()
                .id(1).titre("Dune")
                .totalExemplaires(3)
                .exemplairesDisponibles(3)
                .build();

        reservation = Reservation.builder()
                .id(1)
                .user(user)
                .book(book)
                .reservationDate(LocalDateTime.now())
                .rankWaitingList(1)
                .status(EN_ATTENTE)
                .build();
    }

    // ── createReservation ──────────────────────────────────────────────────────

    @Test
    void createReservation_success_createsReservationWithCorrectRank() {
        ReservationRequest request = new ReservationRequest();
        request.setBookId(1);

        ReservationResponse expected = new ReservationResponse();
        expected.setStatus("EN_ATTENTE");
        expected.setRankWaitingList(2);

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(1, 1, ACTIVE)).thenReturn(false);
        when(reservationRepository.countByUserIdAndStatusIn(1, ACTIVE)).thenReturn(2);
        when(reservationRepository.countByBookIdAndStatus(1, EN_ATTENTE)).thenReturn(1);
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationMapper.toResponse(any())).thenReturn(expected);

        ReservationResponse result = reservationService.createReservation("jean@test.com", request);

        assertThat(result.getStatus()).isEqualTo("EN_ATTENTE");
        assertThat(result.getRankWaitingList()).isEqualTo(2);
    }

    @Test
    void createReservation_rankIsNextInQueue() {
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(1, 1, ACTIVE)).thenReturn(false);
        when(reservationRepository.countByUserIdAndStatusIn(1, ACTIVE)).thenReturn(1);
        when(reservationRepository.countByBookIdAndStatus(1, EN_ATTENTE)).thenReturn(3);
        when(reservationMapper.toResponse(any())).thenReturn(new ReservationResponse());

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        when(reservationRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        reservationService.createReservation("jean@test.com", request(1));

        assertThat(captor.getValue().getRankWaitingList()).isEqualTo(4);
    }

    @Test
    void createReservation_noExemplairesDisponibles_throwsBadRequest() {
        book.setExemplairesDisponibles(0);

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> reservationService.createReservation("jean@test.com", request(1)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createReservation_alreadyHasActiveReservation_throwsConflict() {
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(1, 1, ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation("jean@test.com", request(1)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createReservation_maxFiveActiveReservations_throwsConflict() {
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(1, 1, ACTIVE)).thenReturn(false);
        when(reservationRepository.countByUserIdAndStatusIn(1, ACTIVE)).thenReturn(5);

        assertThatThrownBy(() -> reservationService.createReservation("jean@test.com", request(1)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createReservation_userNotFound_throws404() {
        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation("inconnu@test.com", request(1)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createReservation_bookNotFound_throws404() {
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation("jean@test.com", request(99)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── getMyReservations ──────────────────────────────────────────────────────

    @Test
    void getMyReservations_returnsUserReservations() {
        ReservationResponse response = new ReservationResponse();
        response.setId(1);

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserId(1)).thenReturn(List.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(response);

        List<ReservationResponse> result = reservationService.getMyReservations("jean@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
    }

    @Test
    void getMyReservations_userNotFound_throws404() {
        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getMyReservations("inconnu@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── getAllReservations ─────────────────────────────────────────────────────

    @Test
    void getAllReservations_returnsAllReservations() {
        ReservationResponse response = new ReservationResponse();
        response.setId(1);

        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(response);

        List<ReservationResponse> result = reservationService.getAllReservations();

        assertThat(result).hasSize(1);
    }

    // ── cancelReservation ──────────────────────────────────────────────────────

    @Test
    void cancelReservation_byOwner_cancelsAndShiftsRanks() {
        Reservation r2 = Reservation.builder()
                .id(2).user(user).book(book)
                .rankWaitingList(2).status(EN_ATTENTE).build();

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.findByBookIdAndStatusAndRankWaitingListGreaterThan(1, EN_ATTENTE, 1))
                .thenReturn(List.of(r2));

        reservationService.cancelReservation("jean@test.com", 1, false);

        assertThat(reservation.getStatus()).isEqualTo(ANNULEE);
        assertThat(r2.getRankWaitingList()).isEqualTo(1);
        verify(reservationRepository).save(reservation);
        verify(reservationRepository).saveAll(List.of(r2));
    }

    @Test
    void cancelReservation_byStaff_skipsOwnerCheck() {
        User anotherUser = User.builder().id(99).email("other@test.com").build();
        Reservation otherReservation = Reservation.builder()
                .id(5).user(anotherUser).book(book)
                .rankWaitingList(1).status(EN_ATTENTE).build();

        when(reservationRepository.findById(5)).thenReturn(Optional.of(otherReservation));
        when(reservationRepository.findByBookIdAndStatusAndRankWaitingListGreaterThan(1, EN_ATTENTE, 1))
                .thenReturn(List.of());

        reservationService.cancelReservation("libraire@test.com", 5, true);

        assertThat(otherReservation.getStatus()).isEqualTo(ANNULEE);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void cancelReservation_notOwner_throwsForbidden() {
        User anotherUser = User.builder().id(2).email("autre@test.com").build();

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("autre@test.com")).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> reservationService.cancelReservation("autre@test.com", 1, false))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void cancelReservation_alreadyAnnulee_throwsBadRequest() {
        reservation.setStatus(ANNULEE);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> reservationService.cancelReservation("jean@test.com", 1, false))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void cancelReservation_reservationNotFound_throws404() {
        when(reservationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelReservation("jean@test.com", 99, false))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── validateReservation ────────────────────────────────────────────────────

    @Test
    void validateReservation_enAttente_createsLoanAndSetsDisponible() {
        Reservation r2 = Reservation.builder()
                .id(2).user(user).book(book)
                .rankWaitingList(2).status(EN_ATTENTE).build();

        LoanResponse loanResponse = LoanResponse.builder().id(10).build();
        ReservationResponse expected = new ReservationResponse();
        expected.setStatus("DISPONIBLE");

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(loanService.borrowBook(1, 1)).thenReturn(loanResponse);
        when(reservationRepository.findByBookIdAndStatusAndRankWaitingListGreaterThan(1, EN_ATTENTE, 1))
                .thenReturn(List.of(r2));
        when(reservationMapper.toResponse(reservation)).thenReturn(expected);

        ReservationResponse result = reservationService.validateReservation(1);

        assertThat(reservation.getStatus()).isEqualTo(DISPONIBLE);
        assertThat(r2.getRankWaitingList()).isEqualTo(1);
        verify(loanService).borrowBook(1, 1);
    }

    @Test
    void validateReservation_notEnAttente_throwsBadRequest() {
        reservation.setStatus(DISPONIBLE);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.validateReservation(1))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validateReservation_reservationNotFound_throws404() {
        when(reservationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.validateReservation(99))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ReservationRequest request(int bookId) {
        ReservationRequest req = new ReservationRequest();
        req.setBookId(bookId);
        return req;
    }
}
