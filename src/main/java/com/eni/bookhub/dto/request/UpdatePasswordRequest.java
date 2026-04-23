package com.eni.bookhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdatePasswordRequest {
    @NotBlank
    private String ancienMotDePasse;

    @NotBlank
    @Size(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "Le mot de passe doit contenir au moins 1 majuscule, 1 minuscule, 1 chiffre et 1 caractère spécial"
    )
    private String nouveauMotDePasse;
}
