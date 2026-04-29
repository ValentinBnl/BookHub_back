package com.eni.bookhub.repository;

import com.eni.bookhub.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    Page<Book> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titre,
            String auteur,
            String isbn,
            Pageable pageable);

    List<Book> findByUrlCouvertureIsNull();

    @Query("SELECT MIN(YEAR(b.dateParution)) FROM Book b WHERE b.dateParution IS NOT NULL")
    Integer findMinYear();

    @Query("SELECT COALESCE(SUM(b.totalExemplaires), 0) FROM Book b")
    long sumTotalExemplaires();

    @Query("SELECT COALESCE(SUM(b.exemplairesDisponibles), 0) FROM Book b")
    long sumExemplairesDisponibles();

    @Query("SELECT MAX(YEAR(b.dateParution)) FROM Book b WHERE b.dateParution IS NOT NULL")
    Integer findMaxYear();

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.urlCouverture = :url WHERE b.id = :id")
    void updateCoverUrl(@Param("id") Integer id, @Param("url") String url);
}
