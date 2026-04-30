package com.eni.bookhub.mapper;

import com.eni.bookhub.dto.response.LoanResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Loan;
import com.eni.bookhub.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoanMapperTest {

    private final LoanMapper mapper = new LoanMapper();

    @Test
    void toResponse_mapsAllFields() {
        Book book = Book.builder().id(1).titre("Dune").auteur("Frank Herbert").dateParution(LocalDate.of(1965, 8, 1)).build();
        User user = User.builder().id(1).nom("Garcia").prenom("Alex").build();
        LocalDateTime borrow = LocalDateTime.of(2024, 3, 1, 9, 0);
        LocalDateTime due = LocalDateTime.of(2024, 3, 15, 9, 0);

        Loan loan = Loan.builder()
                .id(10)
                .livre(book)
                .utilisateur(user)
                .dateEmprunt(borrow)
                .dateRetourPrevue(due)
                .statut("EN COURS")
                .build();

        LoanResponse response = mapper.toResponse(loan);

        assertThat(response.getId()).isEqualTo(10);
        assertThat(response.getTitre()).isEqualTo("Dune");
        assertThat(response.getDateEmprunt()).isEqualTo(borrow.toString());
        assertThat(response.getDateRetourPrevue()).isEqualTo(due.toString());
        assertThat(response.getStatut()).isEqualTo("EN COURS");
        assertThat(response.getNom()).isEqualTo("Garcia");
        assertThat(response.getPrenom()).isEqualTo("Alex");
    }

    @Test
    void toResponse_statusRendu_mapsCorrectly() {
        Book book = Book.builder().id(2).titre("1984").auteur("George Orwell").dateParution(LocalDate.of(1949, 6, 8)).build();
        Loan loan = Loan.builder()
                .id(20)
                .livre(book)
                .utilisateur(User.builder().id(1).nom("Orwell").prenom("George").build())
                .dateEmprunt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .dateRetourPrevue(LocalDateTime.of(2024, 1, 15, 10, 0))
                .statut("RENDU")
                .build();

        LoanResponse response = mapper.toResponse(loan);

        assertThat(response.getStatut()).isEqualTo("RENDU");
        assertThat(response.getTitre()).isEqualTo("1984");
    }
}
