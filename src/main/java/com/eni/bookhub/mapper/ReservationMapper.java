package com.eni.bookhub.mapper;

import com.eni.bookhub.dto.response.ReservationResponse;
import com.eni.bookhub.entity.Reservation;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ReservationMapper {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReservationResponse toResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getPrenom() + " " + r.getUser().getNom())
                .bookId(r.getBook().getId())
                .urlCouverture(r.getBook().getUrlCouverture())
                .bookTitle(r.getBook().getTitre())
                .reservationDate(r.getReservationDate().format(FMT))
                .rankWaitingList(r.getRankWaitingList())
                .status(r.getStatus().name())
                .build();
    }
}
