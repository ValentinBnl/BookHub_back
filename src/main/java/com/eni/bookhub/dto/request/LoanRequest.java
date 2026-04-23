package com.eni.bookhub.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest {

    private Integer userId;
    private Integer bookId;
}