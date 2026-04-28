package com.eni.bookhub.repository;

import com.eni.bookhub.AbstractIntegrationTest;
import com.eni.bookhub.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .nom("Dupont").prenom("Jean")
                .email("jean@test.com")
                .telephone("0600000000")
                .motDePasse("$2a$04$hash")
                .role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .build());
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        Optional<User> result = userRepository.findByEmail("jean@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getNom()).isEqualTo("Dupont");
    }

    @Test
    void findByEmail_unknownEmail_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("inconnu@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        assertThat(userRepository.existsByEmail("jean@test.com")).isTrue();
    }

    @Test
    void existsByEmail_unknownEmail_returnsFalse() {
        assertThat(userRepository.existsByEmail("inconnu@test.com")).isFalse();
    }

    @Test
    void existsByTelephone_existingPhone_returnsTrue() {
        assertThat(userRepository.existsByTelephone("0600000000")).isTrue();
    }

    @Test
    void existsByTelephone_unknownPhone_returnsFalse() {
        assertThat(userRepository.existsByTelephone("0699999999")).isFalse();
    }

    @Test
    void existsByTelephoneAndIdNot_sameUserSamePhone_returnsFalse() {
        // Même téléphone, même ID → pas de conflit
        assertThat(userRepository.existsByTelephoneAndIdNot("0600000000", user.getId())).isFalse();
    }

    @Test
    void existsByTelephoneAndIdNot_anotherUserSamePhone_returnsTrue() {
        // Un autre utilisateur a ce téléphone → conflit
        userRepository.save(User.builder()
                .nom("Martin").prenom("Pierre")
                .email("pierre@test.com")
                .telephone("0611111111")
                .motDePasse("$2a$04$hash")
                .role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .build());

        assertThat(userRepository.existsByTelephoneAndIdNot("0611111111", user.getId())).isTrue();
    }
}
