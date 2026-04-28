package com.eni.bookhub.mapper;

import com.eni.bookhub.dto.response.LoanResponse;
import com.eni.bookhub.entity.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .titre(loan.getLivre().getTitre())
                .dateEmprunt(loan.getDateEmprunt().toString())
                .dateRetourPrevue(loan.getDateRetourPrevue().toString())
                .statut(loan.getStatut())
                .build();
    }
}
