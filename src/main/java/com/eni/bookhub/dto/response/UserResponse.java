package com.eni.bookhub.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String role;
    private LocalDateTime dateCreation;
}
