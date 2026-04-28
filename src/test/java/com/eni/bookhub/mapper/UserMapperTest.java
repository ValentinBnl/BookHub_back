package com.eni.bookhub.mapper;

import com.eni.bookhub.dto.request.RegisterRequest;
import com.eni.bookhub.dto.response.UserResponse;
import com.eni.bookhub.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    @Test
    void toResponse_mapsAllFields() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 0);
        User user = User.builder()
                .id(1)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@email.com")
                .telephone("0612345678")
                .role(User.Role.UTILISATEUR)
                .dateCreation(created)
                .build();

        UserResponse response = mapper.toResponse(user);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getNom()).isEqualTo("Dupont");
        assertThat(response.getPrenom()).isEqualTo("Jean");
        assertThat(response.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(response.getTelephone()).isEqualTo("0612345678");
        assertThat(response.getRole()).isEqualTo("UTILISATEUR");
        assertThat(response.getDateCreation()).isEqualTo(created);
    }

    @Test
    void toResponse_adminRole_returnsAdminRoleAsString() {
        User admin = User.builder()
                .id(2)
                .nom("Admin")
                .prenom("Super")
                .email("admin@email.com")
                .telephone("0600000000")
                .role(User.Role.ADMIN)
                .dateCreation(LocalDateTime.now())
                .build();

        UserResponse response = mapper.toResponse(admin);

        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void toEntity_setsCorrectFields() {
        RegisterRequest request = new RegisterRequest();
        request.setNom("Dupont");
        request.setPrenom("Jean");
        request.setEmail("jean.dupont@email.com");
        request.setTelephone("0612345678");
        request.setMotDePasse("plaintext");

        LocalDateTime before = LocalDateTime.now();
        User user = mapper.toEntity(request, "encodedPassword");
        LocalDateTime after = LocalDateTime.now();

        assertThat(user.getNom()).isEqualTo("Dupont");
        assertThat(user.getPrenom()).isEqualTo("Jean");
        assertThat(user.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(user.getTelephone()).isEqualTo("0612345678");
        assertThat(user.getMotDePasse()).isEqualTo("encodedPassword");
        assertThat(user.getDateCreation()).isBetween(before, after);
    }

    @Test
    void toEntity_alwaysSetsRoleUtilisateur() {
        RegisterRequest request = new RegisterRequest();
        request.setNom("Hack");
        request.setPrenom("Er");
        request.setEmail("hacker@evil.com");
        request.setTelephone("0000000000");
        request.setMotDePasse("Hack@1234567890");

        User user = mapper.toEntity(request, "encoded");

        assertThat(user.getRole()).isEqualTo(User.Role.UTILISATEUR);
    }

    @Test
    void toEntity_storesEncodedPasswordNotRaw() {
        RegisterRequest request = new RegisterRequest();
        request.setNom("Test");
        request.setPrenom("User");
        request.setEmail("test@test.com");
        request.setTelephone("0611111111");
        request.setMotDePasse("RawPassword@1");

        User user = mapper.toEntity(request, "$2a$12$hashed");

        assertThat(user.getMotDePasse()).isEqualTo("$2a$12$hashed");
        assertThat(user.getMotDePasse()).isNotEqualTo("RawPassword@1");
    }
}
