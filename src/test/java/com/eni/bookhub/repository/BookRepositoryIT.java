package com.eni.bookhub.repository;

import com.eni.bookhub.AbstractIntegrationTest;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Category;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    private Category category;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(Category.builder().nom("Roman").build());
    }

    private Book saveBook(String titre, String isbn, LocalDate date, String coverUrl) {
        return bookRepository.save(Book.builder()
                .titre(titre)
                .auteur("Auteur Test")
                .isbn(isbn)
                .dateParution(date)
                .nombrePages(300)
                .description("Description")
                .urlCouverture(coverUrl)
                .totalExemplaires(5)
                .exemplairesDisponibles(5)
                .categorie(category)
                .build());
    }

    @Test
    void findMinYear_returnsEarliestPublicationYear() {
        saveBook("Ancien", "ISBN-001", LocalDate.of(1950, 1, 1), null);
        saveBook("Recent", "ISBN-002", LocalDate.of(2020, 6, 15), null);

        Integer min = bookRepository.findMinYear();

        assertThat(min).isEqualTo(1950);
    }

    @Test
    void findMaxYear_returnsLatestPublicationYear() {
        saveBook("Ancien", "ISBN-003", LocalDate.of(1980, 3, 10), null);
        saveBook("Recent", "ISBN-004", LocalDate.of(2023, 11, 1), null);

        Integer max = bookRepository.findMaxYear();

        assertThat(max).isEqualTo(2023);
    }

    @Test
    void findMinYear_emptyTable_returnsNull() {
        assertThat(bookRepository.findMinYear()).isNull();
    }

    @Test
    void findByUrlCouvertureIsNull_returnsBooksWithoutCover() {
        saveBook("Sans couverture", "ISBN-005", LocalDate.of(2000, 1, 1), null);
        saveBook("Avec couverture", "ISBN-006", LocalDate.of(2000, 1, 1), "http://cover.jpg");

        List<Book> result = bookRepository.findByUrlCouvertureIsNull();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitre()).isEqualTo("Sans couverture");
    }

    @Test
    void updateCoverUrl_setsUrlOnBook() {
        Book book = saveBook("Test", "ISBN-007", LocalDate.of(2010, 5, 1), null);

        bookRepository.updateCoverUrl(book.getId(), "http://new-cover.jpg");

        // La requête JPQL @Modifying bypass le cache de premier niveau de JPA.
        // On vide le cache pour forcer un rechargement depuis la base.
        entityManager.flush();
        entityManager.clear();

        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updated.getUrlCouverture()).isEqualTo("http://new-cover.jpg");
    }

    @Test
    void findById_existingBook_returnsBook() {
        Book saved = saveBook("Dune", "ISBN-008", LocalDate.of(1965, 8, 1), null);

        assertThat(bookRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Book::getTitre)
                .isEqualTo("Dune");
    }
}
