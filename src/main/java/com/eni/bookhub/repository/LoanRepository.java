package com.eni.bookhub.repository;

import com.eni.bookhub.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {
    boolean existsByLivreIdAndStatutIn(Integer livreId, List<String> statuts);
}
