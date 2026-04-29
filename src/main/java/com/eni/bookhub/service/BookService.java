package com.eni.bookhub.service;

import com.eni.bookhub.dto.request.BookRequest;
import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.dto.response.BookStatsResponse;
import com.eni.bookhub.dto.response.BookSummaryResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Category;
import com.eni.bookhub.mapper.BookMapper;
import com.eni.bookhub.repository.BookRepository;
import com.eni.bookhub.repository.CategoryRepository;
import com.eni.bookhub.repository.LoanRepository;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final CategoryRepository categoryRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, BookMapper bookMapper,
                       CategoryRepository categoryRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.categoryRepository = categoryRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(bookMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> search(String query, String categorie, Boolean disponible,
                                            Integer anneeMin, Integer anneeMax, Pageable pageable) {
        Specification<Book> spec = (root, cq, cb) -> cb.conjunction();

        if (query != null && !query.isBlank()) {
            String like = "%" + query.toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("titre")),  like),
                    cb.like(cb.lower(root.get("auteur")), like),
                    cb.like(cb.lower(root.get("isbn")),   like)
            ));
        }

        if (categorie != null && !categorie.isBlank()) {
            spec = spec.and((root, cq, cb) ->
                    cb.equal(cb.lower(root.get("categorie").get("nom")), categorie.toLowerCase())
            );
        }

        if (disponible != null) {
            spec = spec.and((root, cq, cb) -> disponible
                    ? cb.greaterThan(root.get("exemplairesDisponibles"), 0)
                    : cb.equal(root.get("exemplairesDisponibles"), 0));
        }

        if (anneeMin != null) {
            spec = spec.and((root, cq, cb) -> {
                Expression<Integer> year = cb.function("YEAR", Integer.class, root.get("dateParution"));
                return cb.greaterThanOrEqualTo(year, anneeMin);
            });
        }

        if (anneeMax != null) {
            spec = spec.and((root, cq, cb) -> {
                Expression<Integer> year = cb.function("YEAR", Integer.class, root.get("dateParution"));
                return cb.lessThanOrEqualTo(year, anneeMax);
            });
        }

        return bookRepository.findAll(spec, pageable).map(bookMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public int[] getYearRange() {
        int min = bookRepository.findMinYear() != null ? bookRepository.findMinYear() : 1800;
        int max = bookRepository.findMaxYear() != null ? bookRepository.findMaxYear() : java.time.Year.now().getValue();
        return new int[]{ min, max };
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Integer id) {
        return bookRepository.findById(id)
                .map(bookMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Livre introuvable"));
    }

    @Transactional(readOnly = true)
    public BookStatsResponse getStats() {
        long totalTitres = bookRepository.count();
        long totalExemplaires = bookRepository.sumTotalExemplaires();
        long disponibles = bookRepository.sumExemplairesDisponibles();
        return new BookStatsResponse(totalTitres, totalExemplaires, disponibles, totalExemplaires - disponibles);
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        Category categorie = categoryRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));
        Book book = Book.builder()
                .titre(request.getTitre())
                .auteur(request.getAuteur())
                .isbn(request.getIsbn())
                .dateParution(request.getDateParution())
                .nombrePages(request.getNombrePages())
                .description(request.getDescription())
                .urlCouverture(request.getUrlCouverture())
                .totalExemplaires(request.getTotalExemplaires())
                .exemplairesDisponibles(request.getTotalExemplaires())
                .categorie(categorie)
                .build();
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse updateBook(Integer id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livre introuvable"));
        Category categorie = categoryRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));
        book.setTitre(request.getTitre());
        book.setAuteur(request.getAuteur());
        book.setIsbn(request.getIsbn());
        book.setDateParution(request.getDateParution());
        book.setNombrePages(request.getNombrePages());
        book.setDescription(request.getDescription());
        book.setUrlCouverture(request.getUrlCouverture());
        book.setTotalExemplaires(request.getTotalExemplaires());
        book.setCategorie(categorie);
        return bookMapper.toResponse(book);
    }

    @Transactional
    public void deleteBook(Integer id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livre introuvable"));
        if (loanRepository.existsByLivreIdAndStatutIn(id, List.of("EN COURS", "EN RETARD")))
            throw new RuntimeException("Impossible de supprimer : des emprunts sont en cours");
        bookRepository.delete(book);
    }
}
