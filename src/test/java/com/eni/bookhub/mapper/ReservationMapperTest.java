package com.eni.bookhub.mapper;

import com.eni.bookhub.dto.response.ReservationResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Reservation;
import com.eni.bookhub.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMapperTest {

    private final ReservationMapper mapper = new ReservationMapper();

    @Test
    void toResponse_mapsAllFields() {
        User user = User.builder().id(2).prenom("Marie").nom("Curie").build();
        Book book = Book.builder().id(5).titre("Cosmos").build();
        LocalDateTime date = LocalDateTime.of(2024, 6, 15, 14, 30);

        Reservation reservation = Reservation.builder()
                .id(99)
                .user(user)
                .book(book)
                .reservationDate(date)
                .rankWaitingList(3)
                .status(Reservation.Status.EN_ATTENTE)
                .build();

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response.getId()).isEqualTo(99);
        assertThat(response.getUserId()).isEqualTo(2);
        assertThat(response.getUserName()).isEqualTo("Marie Curie");
        assertThat(response.getBookId()).isEqualTo(5);
        assertThat(response.getBookTitle()).isEqualTo("Cosmos");
        assertThat(response.getReservationDate()).isEqualTo("15/06/2024 14:30");
        assertThat(response.getRankWaitingList()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo("EN_ATTENTE");
    }

    @Test
    void toResponse_statusDisponible_mapsCorrectly() {
        User user = User.builder().id(1).prenom("Paul").nom("Martin").build();
        Book book = Book.builder().id(1).titre("Livre").build();

        Reservation reservation = Reservation.builder()
                .id(1)
                .user(user)
                .book(book)
                .reservationDate(LocalDateTime.of(2024, 1, 1, 8, 0))
                .rankWaitingList(1)
                .status(Reservation.Status.DISPONIBLE)
                .build();

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response.getStatus()).isEqualTo("DISPONIBLE");
    }

    @Test
    void toResponse_userNameIsFirstnameThenLastname() {
        User user = User.builder().id(3).prenom("Alice").nom("Wonderland").build();
        Book book = Book.builder().id(3).titre("Any Book").build();

        Reservation reservation = Reservation.builder()
                .id(3)
                .user(user)
                .book(book)
                .reservationDate(LocalDateTime.of(2024, 12, 31, 23, 59))
                .rankWaitingList(1)
                .status(Reservation.Status.ANNULEE)
                .build();

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response.getUserName()).isEqualTo("Alice Wonderland");
    }

    @Test
    void toResponse_dateFormattedCorrectly() {
        User user = User.builder().id(1).prenom("A").nom("B").build();
        Book book = Book.builder().id(1).titre("T").build();
        LocalDateTime date = LocalDateTime.of(2024, 1, 5, 8, 5);

        Reservation reservation = Reservation.builder()
                .id(1).user(user).book(book)
                .reservationDate(date)
                .rankWaitingList(1)
                .status(Reservation.Status.EN_ATTENTE)
                .build();

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response.getReservationDate()).isEqualTo("05/01/2024 08:05");
    }
}
