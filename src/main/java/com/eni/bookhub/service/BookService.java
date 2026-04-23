package com.eni.bookhub.service;

import com.eni.bookhub.dto.request.BookRequest;
import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.dto.response.BookSummaryResponse;
import com.eni.bookhub.entity.Book;
import com.eni.bookhub.entity.Category;
import com.eni.bookhub.repository.BookRepository;
import com.eni.bookhub.repository.CategoryRepository;
import com.eni.bookhub.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.loanRepository = loanRepository;
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(book);
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookSummaryResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> search(String query, String categorie, Boolean disponible, Pageable pageable) {
        Page<Book> page = bookRepository
                .findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                        query, query, query, pageable
                );

        List<Book> filtered = page.getContent();

        if (categorie != null && !categorie.isBlank()) {
            filtered = filtered.stream()
                    .filter(book -> book.getCategorie() != null &&
                            book.getCategorie().getNom().equalsIgnoreCase(categorie))
                    .collect(Collectors.toList());
        }

        if (disponible != null) {
            filtered = filtered.stream()
                    .filter(book -> disponible
                            ? book.getExemplairesDisponibles() > 0
                            : book.getExemplairesDisponibles() == 0)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(filtered, pageable, filtered.size()).map(BookSummaryResponse::new);
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Integer id) {
        return bookRepository.findById(id)
                .map(BookResponse::new)
                .orElseThrow(() -> new RuntimeException("Livre introuvable"));
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
        return toResponse(bookRepository.save(book));
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
        return toResponse(book);
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
