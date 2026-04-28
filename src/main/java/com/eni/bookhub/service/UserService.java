package com.eni.bookhub.service;

import com.eni.bookhub.dto.request.UpdatePasswordRequest;
import com.eni.bookhub.dto.request.UpdateProfileRequest;
import com.eni.bookhub.dto.response.UserResponse;
import com.eni.bookhub.entity.User;
import com.eni.bookhub.mapper.UserMapper;
import com.eni.bookhub.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!user.getTelephone().equals(request.getTelephone())
                && userRepository.existsByTelephoneAndIdNot(request.getTelephone(), user.getId()))
            throw new RuntimeException("Téléphone déjà utilisé");

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getAncienMotDePasse(), user.getMotDePasse()))
            throw new RuntimeException("Ancien mot de passe incorrect");

        user.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        userRepository.delete(user);
    }
}
