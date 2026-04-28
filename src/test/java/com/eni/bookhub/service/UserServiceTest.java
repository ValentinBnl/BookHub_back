package com.eni.bookhub.service;

import com.eni.bookhub.dto.request.UpdatePasswordRequest;
import com.eni.bookhub.dto.request.UpdateProfileRequest;
import com.eni.bookhub.dto.response.UserResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

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

    // --- getProfile ---

    @Test
    void getProfile_userFound_returnsResponse() {
        UserResponse expected = UserResponse.builder().id(1).email("jean@test.com").build();
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse result = userService.getProfile("jean@test.com");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getProfile_userNotFound_throwsException() {
        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("inconnu@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    // --- updateProfile ---

    @Test
    void updateProfile_samePhone_updatesWithoutConflictCheck() {
        UpdateProfileRequest request = mock(UpdateProfileRequest.class);
        when(request.getNom()).thenReturn("Martin");
        when(request.getPrenom()).thenReturn("Pierre");
        when(request.getTelephone()).thenReturn("0600000000"); // même téléphone

        UserResponse expected = UserResponse.builder().id(1).nom("Martin").build();
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toResponse(any())).thenReturn(expected);

        UserResponse result = userService.updateProfile("jean@test.com", request);

        assertThat(result).isEqualTo(expected);
        verify(userRepository, never()).existsByTelephoneAndIdNot(any(), any());
    }

    @Test
    void updateProfile_newPhoneAlreadyUsed_throwsException() {
        UpdateProfileRequest request = mock(UpdateProfileRequest.class);
        when(request.getTelephone()).thenReturn("0611111111"); // téléphone différent

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByTelephoneAndIdNot("0611111111", 1)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile("jean@test.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Téléphone");
    }

    @Test
    void updateProfile_newPhoneAvailable_updatesSuccessfully() {
        UpdateProfileRequest request = mock(UpdateProfileRequest.class);
        when(request.getNom()).thenReturn("Martin");
        when(request.getPrenom()).thenReturn("Pierre");
        when(request.getTelephone()).thenReturn("0622222222");

        UserResponse expected = UserResponse.builder().id(1).build();
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByTelephoneAndIdNot("0622222222", 1)).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toResponse(any())).thenReturn(expected);

        UserResponse result = userService.updateProfile("jean@test.com", request);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getNom()).isEqualTo("Martin");
        assertThat(user.getPrenom()).isEqualTo("Pierre");
        assertThat(user.getTelephone()).isEqualTo("0622222222");
    }

    @Test
    void updateProfile_userNotFound_throwsException() {
        UpdateProfileRequest request = mock(UpdateProfileRequest.class);
        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile("inconnu@test.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    // --- updatePassword ---

    @Test
    void updatePassword_correctOldPassword_updatesPassword() {
        UpdatePasswordRequest request = mock(UpdatePasswordRequest.class);
        when(request.getAncienMotDePasse()).thenReturn(RAW_PASSWORD);
        when(request.getNouveauMotDePasse()).thenReturn("NewPass@12345678");

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updatePassword("jean@test.com", request);

        verify(userRepository).save(user);
        assertThat(user.getMotDePasse()).isNotEqualTo(ENCODED_PASSWORD);
    }

    @Test
    void updatePassword_wrongOldPassword_throwsException() {
        UpdatePasswordRequest request = mock(UpdatePasswordRequest.class);
        when(request.getAncienMotDePasse()).thenReturn("MauvaisMotDePasse@1");

        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updatePassword("jean@test.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ancien mot de passe incorrect");
    }

    @Test
    void updatePassword_userNotFound_throwsException() {
        UpdatePasswordRequest request = mock(UpdatePasswordRequest.class);
        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePassword("inconnu@test.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    // --- deleteAccount ---

    @Test
    void deleteAccount_userFound_deletesUser() {
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(user));

        userService.deleteAccount("jean@test.com");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteAccount_userNotFound_throwsException() {
        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount("inconnu@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }
}
