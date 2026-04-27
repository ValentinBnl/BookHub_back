package com.eni.bookhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Integer id;
    private Integer userId;
    private String userName;
    private Integer bookId;
    private String bookTitle;
    private String reservationDate;
    private Integer rankWaitingList;
    private String status;
}
