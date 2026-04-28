package com.eni.bookhub.service;

import com.eni.bookhub.dto.request.LoginRequest;
import com.eni.bookhub.dto.request.RegisterRequest;
import com.eni.bookhub.dto.response.AuthResponse;
import com.eni.bookhub.entity.User;
import com.eni.bookhub.mapper.UserMapper;
import com.eni.bookhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private UserMapper userMapper;

    @InjectMocks private AuthService authService;

    // BCrypt strength 4 pour la vitesse ; matches() vérifie le coût depuis le hash stocké
    private static final BCryptPasswordEncoder TEST_ENCODER = new BCryptPasswordEncoder(4);
    private static final String RAW_PASSWORD = "Test@1234567890";
    private static final String ENCODED_PASSWORD = TEST_ENCODER.encode(RAW_PASSWORD);

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@test.com")
                .telephone("0600000000")
                .motDePasse(ENCODED_PASSWORD)
                .role(User.Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .build();
    }

    // --- register ---

    @Test
    void register_success_returnsTokenAndUserInfo() {
        RegisterRequest request = new RegisterRequest();
        request.setNom("Dupont");
        request.setPrenom("Jean");
        request.setEmail("jean@test.com");
        request.setTelephone("0600000000");
        request.setMotDePasse(RAW_PASSWORD);

        when(userRepository.existsByEmail("jean@test.com")).thenReturn(false);
        when(userRepository.existsByTelephone("0600000000")).thenReturn(false);
        when(userMapper.toEntity(any(), any())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(jwtService.generateToken("jean@test.com", "UTILISATEUR")).thenReturn("token123");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getEmail()).isEqualTo("jean@test.com");
        assertThat(response.getRole()).isEqualTo("UTILISATEUR");
    }

    @Test
    void register_emailAlreadyUsed_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("jean@test.com");
        request.setTelephone("0600000000");

        when(userRepository.existsByEmail("jean@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void register_phoneAlreadyUsed_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("nouveau@test.com");
        request.setTelephone("0600000000");

        when(userRepository.existsByEmail("nouveau@test.com")).thenReturn(false);
        when(userRepository.existsByTelephone("0600000000")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Téléphone");
    }

    // --- login ---

    @Test
    void login_correctCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jean@test.com");
        request.setMotDePasse(RAW_PASSWORD);

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("jean@test.com", "UTILISATEUR")).thenReturn("jwtToken");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwtToken");
        assertThat(response.getEmail()).isEqualTo("jean@test.com");
        assertThat(response.getRole()).isEqualTo("UTILISATEUR");
    }

    @Test
    void login_unknownEmail_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("inconnu@test.com");
        request.setMotDePasse(RAW_PASSWORD);

        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("incorrect");
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jean@test.com");
        request.setMotDePasse("MauvaisMotDePasse@1");

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("incorrect");
    }
}
