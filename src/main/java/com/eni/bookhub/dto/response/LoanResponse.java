package com.eni.bookhub.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {

    private Integer id;
    private String titre;
    private String dateEmprunt;
    private String dateRetourPrevue;
    private String statut;
}