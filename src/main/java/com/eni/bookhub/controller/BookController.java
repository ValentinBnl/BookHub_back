package com.eni.bookhub.controller;

import com.eni.bookhub.dto.response.BookResponse;
import com.eni.bookhub.service.BookService;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    //  Liste des livres
    @GetMapping
    public Page<BookResponse> getAllBooks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "titre") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return service.getAllBooks(pageable);
    }

    //  Recherche (titre OR auteur OR isbn + filtres)
    @GetMapping("/search")
    public Page<BookResponse> searchBooks(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "categorie", required = false) String categorie,
            @RequestParam(name = "disponible", required = false) Boolean disponible,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "titre") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return service.search(query, categorie, disponible, pageable);
    }

    //  Pagination + tri sécurisé
    private Pageable buildPageable(int page, int size, String sortBy, String direction) {

        List<String> allowedFields = List.of(
                "titre",
                "auteur",
                "isbn",
                "dateParution",
                "nombrePages"
        );

        if (!allowedFields.contains(sortBy)) {
            sortBy = "titre";
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }
}