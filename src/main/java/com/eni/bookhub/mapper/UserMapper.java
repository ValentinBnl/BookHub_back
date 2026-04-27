package com.eni.bookhub.mapper;

import com.eni.bookhub.dto.request.RegisterRequest;
import com.eni.bookhub.dto.response.UserResponse;
import com.eni.bookhub.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole().name())
                .dateCreation(user.getDateCreation())
                .build();
    }

    public User toEntity(RegisterRequest request, String encodedPassword) {
        return User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .motDePasse(encodedPassword)
                .role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .build();
    }
}
