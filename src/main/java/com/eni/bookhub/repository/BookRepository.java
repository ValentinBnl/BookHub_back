package com.eni.bookhub.repository;

import com.eni.bookhub.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Page<Book> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titre,
            String auteur,
            String isbn,
            Pageable pageable
    );
}