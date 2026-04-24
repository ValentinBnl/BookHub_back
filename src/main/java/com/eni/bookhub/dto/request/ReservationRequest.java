package com.eni.bookhub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationRequest {

    @NotNull
    private Integer bookId;
}
