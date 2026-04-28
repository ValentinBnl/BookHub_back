package com.eni.bookhub.repository;

import com.eni.bookhub.AbstractIntegrationTest;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Category;
import com.eni.bookhub.entity.Reservation;
import com.eni.bookhub.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.eni.bookhub.entity.Reservation.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

class ReservationRepositoryIT extends AbstractIntegrationTest {

    @Autowired private ReservationRepository reservationRepository;
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
                .totalExemplaires(5).exemplairesDisponibles(0)
                .categorie(category)
                .build());
    }

    private Reservation saveReservation(User u, Book b, Reservation.Status status, int rank) {
        return reservationRepository.save(Reservation.builder()
                .user(u)
                .book(b)
                .reservationDate(LocalDateTime.now())
                .rankWaitingList(rank)
                .status(status)
                .build());
    }

    @Test
    void findByUserId_returnsAllReservationsForUser() {
        saveReservation(user, book, EN_ATTENTE, 1);

        List<Reservation> result = reservationRepository.findByUserId(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserId_otherUser_returnsEmpty() {
        User otherUser = userRepository.save(User.builder()
                .nom("Martin").prenom("Paul")
                .email("paul@test.com").telephone("0611111111")
                .motDePasse("$2a$04$hash")
                .role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .build());
        saveReservation(otherUser, book, EN_ATTENTE, 1);

        List<Reservation> result = reservationRepository.findByUserId(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUserIdAndBookIdAndStatusIn_activeReservationExists_returnsTrue() {
        saveReservation(user, book, EN_ATTENTE, 1);

        assertThat(reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                user.getId(), book.getId(), List.of(EN_ATTENTE, DISPONIBLE))).isTrue();
    }

    @Test
    void existsByUserIdAndBookIdAndStatusIn_onlyAnnuleeStatus_returnsFalse() {
        saveReservation(user, book, ANNULEE, 1);

        assertThat(reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                user.getId(), book.getId(), List.of(EN_ATTENTE, DISPONIBLE))).isFalse();
    }

    @Test
    void countByBookIdAndStatus_returnsCorrectCount() {
        saveReservation(user, book, EN_ATTENTE, 1);

        User user2 = userRepository.save(User.builder()
                .nom("Leclerc").prenom("Alice")
                .email("alice@test.com").telephone("0622222222")
                .motDePasse("$2a$04$hash")
                .role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .build());
        saveReservation(user2, book, EN_ATTENTE, 2);
        saveReservation(user2, book, ANNULEE, 0);

        int count = reservationRepository.countByBookIdAndStatus(book.getId(), EN_ATTENTE);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByUserIdAndStatusIn_returnsActiveReservationCount() {
        saveReservation(user, book, EN_ATTENTE, 1);

        Book book2 = bookRepository.save(Book.builder()
                .titre("1984").auteur("Orwell")
                .isbn("978-0-451-52493-5")
                .dateParution(LocalDate.of(1949, 6, 8))
                .nombrePages(328).description("Dystopie")
                .totalExemplaires(3).exemplairesDisponibles(0)
                .categorie(book.getCategorie())
                .build());
        saveReservation(user, book2, DISPONIBLE, 1);
        saveReservation(user, book2, ANNULEE, 0);

        int count = reservationRepository.countByUserIdAndStatusIn(user.getId(), List.of(EN_ATTENTE, DISPONIBLE));

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findByBookIdAndStatusAndRankWaitingListGreaterThan_returnsCorrectReservations() {
        saveReservation(user, book, EN_ATTENTE, 1);

        User user2 = userRepository.save(User.builder()
                .nom("X").prenom("Y").email("xy@test.com").telephone("0633333333")
                .motDePasse("$2a$04$hash").role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now()).build());
        saveReservation(user2, book, EN_ATTENTE, 2);

        User user3 = userRepository.save(User.builder()
                .nom("A").prenom("B").email("ab@test.com").telephone("0644444444")
                .motDePasse("$2a$04$hash").role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now()).build());
        saveReservation(user3, book, EN_ATTENTE, 3);

        // Cherche les réservations avec rang > 1 → rangs 2 et 3
        List<Reservation> result = reservationRepository
                .findByBookIdAndStatusAndRankWaitingListGreaterThan(book.getId(), EN_ATTENTE, 1);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Reservation::getRankWaitingList)
                .containsExactlyInAnyOrder(2, 3);
    }

    @Test
    void findByBookIdAndStatusOrderByReservationDateAsc_returnsInOrder() {
        saveReservation(user, book, EN_ATTENTE, 1);

        User user2 = userRepository.save(User.builder()
                .nom("B").prenom("C").email("bc@test.com").telephone("0655555555")
                .motDePasse("$2a$04$hash").role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now()).build());
        saveReservation(user2, book, EN_ATTENTE, 2);

        List<Reservation> result = reservationRepository
                .findByBookIdAndStatusOrderByReservationDateAsc(book.getId(), EN_ATTENTE);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReservationDate())
                .isBeforeOrEqualTo(result.get(1).getReservationDate());
    }
}
