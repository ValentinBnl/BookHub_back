package com.eni.bookhub.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoanResponse {

    private Integer id;
    private String titre;
    private String dateEmprunt;
    private String dateRetourPrevue;
    private String statut;
}