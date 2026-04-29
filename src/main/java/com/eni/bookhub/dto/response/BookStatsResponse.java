package com.eni.bookhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookStatsResponse {
    private long totalTitres;
    private long totalExemplaires;
    private long disponibles;
    private long enPret;
}
