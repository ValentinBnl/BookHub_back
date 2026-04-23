package com.eni.bookhub.dto.response;

import java.time.LocalDateTime;

import com.eni.bookhub.entity.User;

import lombok.Getter;

@Getter
public class UserResponse {
    private final Integer id;
    private final String nom;
    private final String prenom;
    private final String email;
    private final String telephone;
    private final String role;
    private final LocalDateTime dateCreation;

    public UserResponse(User user) {
        this.id = user.getId();
        this.nom = user.getNom();
        this.prenom = user.getPrenom();
        this.email = user.getEmail();
        this.telephone = user.getTelephone();
        this.role = user.getRole().name();
        this.dateCreation = user.getDateCreation();
    }
}
