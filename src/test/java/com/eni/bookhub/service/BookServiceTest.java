package com.eni.bookhub.service;

import com.eni.bookhub.dto.request.BookRequest;
import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.dto.response.BookSummaryResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Category;
import com.eni.bookhub.mapper.BookMapper;
import com.eni.bookhub.repository.BookRepository;
import com.eni.bookhub.repository.CategoryRepository;
import com.eni.bookhub.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private BookMapper bookMapper;
    @Mock private CategoryRepository categoryRepository;
    @Mock private LoanRepository loanRepository;

    @InjectMocks private BookService bookService;

    private Category category;
    private Book book;
    private BookResponse bookResponse;
    private BookSummaryResponse summaryResponse;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1).nom("Roman").build();

        book = Book.builder()
                .id(1)
                .titre("Dune")
                .auteur("Frank Herbert")
                .isbn("978-0-441-17271-9")
                .dateParution(LocalDate.of(1965, 8, 1))
                .nombrePages(412)
                .description("Un roman de science-fiction")
                .totalExemplaires(5)
                .exemplairesDisponibles(3)
                .categorie(category)
                .build();

        bookResponse = BookResponse.builder()
                .id(1).titre("Dune").auteur("Frank Herbert")
                .totalExemplaires(5).exemplairesDisponibles(3).categorie("Roman")
                .build();

        summaryResponse = BookSummaryResponse.builder()
                .id(1).titre("Dune").auteur("Frank Herbert")
                .totalExemplaires(5).exemplairesDisponibles(3).categorie("Roman")
                .build();
    }

    // --- getAllBooks ---

    @Test
    void getAllBooks_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book));
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toSummaryResponse(book)).thenReturn(summaryResponse);

        Page<BookSummaryResponse> result = bookService.getAllBooks(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitre()).isEqualTo("Dune");
    }

    @Test
    void getAllBooks_emptyRepository_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<BookSummaryResponse> result = bookService.getAllBooks(pageable);

        assertThat(result.getContent()).isEmpty();
    }

    // --- search ---

    @Test
    void search_withNullFilters_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(bookMapper.toSummaryResponse(book)).thenReturn(summaryResponse);

        Page<BookSummaryResponse> result = bookService.search(null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_withQuery_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(bookMapper.toSummaryResponse(book)).thenReturn(summaryResponse);

        Page<BookSummaryResponse> result = bookService.search("Dune", null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository).findAll(any(Specification.class), eq(pageable));
    }

    // --- getYearRange ---

    @Test
    void getYearRange_bothPresent_returnsMinAndMax() {
        when(bookRepository.findMinYear()).thenReturn(1990);
        when(bookRepository.findMaxYear()).thenReturn(2024);

        int[] range = bookService.getYearRange();

        assertThat(range[0]).isEqualTo(1990);
        assertThat(range[1]).isEqualTo(2024);
    }

    @Test
    void getYearRange_minNull_returns1800AsDefault() {
        when(bookRepository.findMinYear()).thenReturn(null);
        when(bookRepository.findMaxYear()).thenReturn(2020);

        int[] range = bookService.getYearRange();

        assertThat(range[0]).isEqualTo(1800);
        assertThat(range[1]).isEqualTo(2020);
    }

    @Test
    void getYearRange_maxNull_returnsCurrentYearAsDefault() {
        when(bookRepository.findMinYear()).thenReturn(2000);
        when(bookRepository.findMaxYear()).thenReturn(null);

        int[] range = bookService.getYearRange();

        assertThat(range[0]).isEqualTo(2000);
        assertThat(range[1]).isEqualTo(java.time.Year.now().getValue());
    }

    // --- getById ---

    @Test
    void getById_bookFound_returnsMappedResponse() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.getById(1);

        assertThat(result.getTitre()).isEqualTo("Dune");
    }

    @Test
    void getById_bookNotFound_throwsException() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    // --- createBook ---

    @Test
    void createBook_categoryFound_savesAndReturnsBook() {
        BookRequest request = new BookRequest();
        request.setTitre("Dune");
        request.setAuteur("Frank Herbert");
        request.setIsbn("978-0-441-17271-9");
        request.setDateParution(LocalDate.of(1965, 8, 1));
        request.setNombrePages(412);
        request.setDescription("SF");
        request.setTotalExemplaires(5);
        request.setCategorieId(1);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(bookRepository.save(any())).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.createBook(request);

        assertThat(result.getTitre()).isEqualTo("Dune");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_exemplairesDisponiblesEqualsTotal() {
        BookRequest request = new BookRequest();
        request.setTitre("Test");
        request.setAuteur("Auteur");
        request.setIsbn("123");
        request.setDateParution(LocalDate.now());
        request.setNombrePages(100);
        request.setDescription("desc");
        request.setTotalExemplaires(3);
        request.setCategorieId(1);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookMapper.toResponse(any())).thenReturn(bookResponse);

        bookService.createBook(request);

        verify(bookRepository).save(argThat(b -> b.getExemplairesDisponibles().equals(3)));
    }

    @Test
    void createBook_categoryNotFound_throws404() {
        BookRequest request = new BookRequest();
        request.setCategorieId(99);

        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    // --- updateBook ---

    @Test
    void updateBook_bookAndCategoryFound_updatesAndReturnsBook() {
        BookRequest request = new BookRequest();
        request.setTitre("Nouveau titre");
        request.setAuteur("Nouvel auteur");
        request.setIsbn("new-isbn");
        request.setDateParution(LocalDate.of(2020, 1, 1));
        request.setNombrePages(300);
        request.setDescription("Nouvelle description");
        request.setTotalExemplaires(10);
        request.setCategorieId(1);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(bookMapper.toResponse(book)).thenReturn(bookResponse);

        bookService.updateBook(1, request);

        assertThat(book.getTitre()).isEqualTo("Nouveau titre");
        assertThat(book.getAuteur()).isEqualTo("Nouvel auteur");
        assertThat(book.getNombrePages()).isEqualTo(300);
    }

    @Test
    void updateBook_bookNotFound_throws404() {
        BookRequest request = new BookRequest();
        request.setCategorieId(1);

        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void updateBook_categoryNotFound_throws404() {
        BookRequest request = new BookRequest();
        request.setCategorieId(99);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(1, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    // --- deleteBook ---

    @Test
    void deleteBook_noActiveLoans_deletesBook() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(loanRepository.existsByLivreIdAndStatutIn(1, List.of("EN COURS", "EN RETARD")))
                .thenReturn(false);

        bookService.deleteBook(1);

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_activeLoansExist_throwsException() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(loanRepository.existsByLivreIdAndStatutIn(1, List.of("EN COURS", "EN RETARD")))
                .thenReturn(true);

        assertThatThrownBy(() -> bookService.deleteBook(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("emprunts");
    }

    @Test
    void deleteBook_bookNotFound_throws404() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
