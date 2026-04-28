package com.eni.bookhub.mapper;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.dto.response.BookSummaryResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperTest {

    private BookMapper mapper;
    private Book book;

    @BeforeEach
    void setUp() {
        mapper = new BookMapper();

        Category cat = Category.builder().id(1).nom("Roman").build();
        book = Book.builder()
                .id(1)
                .titre("Les Misérables")
                .auteur("Victor Hugo")
                .isbn("978-0-14-044430-1")
                .dateParution(LocalDate.of(1862, 4, 3))
                .nombrePages(1232)
                .description("Roman historique")
                .urlCouverture("http://cover.jpg")
                .totalExemplaires(5)
                .exemplairesDisponibles(3)
                .categorie(cat)
                .build();
    }

    @Test
    void toResponse_mapsAllFields() {
        BookResponse response = mapper.toResponse(book);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getTitre()).isEqualTo("Les Misérables");
        assertThat(response.getAuteur()).isEqualTo("Victor Hugo");
        assertThat(response.getIsbn()).isEqualTo("978-0-14-044430-1");
        assertThat(response.getDateParution()).isEqualTo(LocalDate.of(1862, 4, 3));
        assertThat(response.getNombrePages()).isEqualTo(1232);
        assertThat(response.getDescription()).isEqualTo("Roman historique");
        assertThat(response.getUrlCouverture()).isEqualTo("http://cover.jpg");
        assertThat(response.getTotalExemplaires()).isEqualTo(5);
        assertThat(response.getExemplairesDisponibles()).isEqualTo(3);
        assertThat(response.getCategorie()).isEqualTo("Roman");
    }

    @Test
    void toResponse_withNullCoverUrl_mapsNull() {
        book.setUrlCouverture(null);

        BookResponse response = mapper.toResponse(book);

        assertThat(response.getUrlCouverture()).isNull();
    }

    @Test
    void toSummaryResponse_mapsAllFields() {
        BookSummaryResponse summary = mapper.toSummaryResponse(book);

        assertThat(summary.getId()).isEqualTo(1);
        assertThat(summary.getTitre()).isEqualTo("Les Misérables");
        assertThat(summary.getAuteur()).isEqualTo("Victor Hugo");
        assertThat(summary.getUrlCouverture()).isEqualTo("http://cover.jpg");
        assertThat(summary.getTotalExemplaires()).isEqualTo(5);
        assertThat(summary.getExemplairesDisponibles()).isEqualTo(3);
        assertThat(summary.getCategorie()).isEqualTo("Roman");
    }

    @Test
    void toSummaryResponse_doesNotIncludeDescriptionOrIsbn() {
        BookSummaryResponse summary = mapper.toSummaryResponse(book);

        assertThat(summary).doesNotHaveToString("isbn")
                .doesNotHaveToString("description");
    }
}
