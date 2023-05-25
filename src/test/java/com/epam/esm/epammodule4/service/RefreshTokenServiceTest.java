package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.exception.TokenRefreshException;
import com.epam.esm.epammodule4.model.entity.RefreshToken;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.RefreshTokenRepository;
import com.epam.esm.epammodule4.repository.UserRepository;
import com.epam.esm.epammodule4.service.implementation.RefreshTokenService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Long USER_ID = 1L;
    @InjectMocks
    private RefreshTokenService subject;
    @Mock
    private UserRepository userRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Test
    void findByToken() {
        String refreshTokenString = "refreshtoken";
        RefreshToken expectedRefreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(User.builder().id(USER_ID).name("User").build())
                .build();

        when(refreshTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(expectedRefreshToken));

        Optional<RefreshToken> actualRefreshToken = subject.findByToken(refreshTokenString);

        assertThat(actualRefreshToken).isEqualTo(Optional.of(expectedRefreshToken));

        verify(refreshTokenRepository).findByToken(refreshTokenString);
        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    void createRefreshToken() {
        User user = User.builder().id(USER_ID).name("User").build();

        String refreshTokenString = "refreshtoken";

        RefreshToken expectedRefreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .build();

        ReflectionTestUtils.setField(subject, "refreshTokenDurationMs", 10L);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(expectedRefreshToken);

        RefreshToken actualRefreshToken = subject.createRefreshToken(USER_ID);

        assertThat(actualRefreshToken).isEqualTo(expectedRefreshToken);

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository, refreshTokenRepository);
    }

    @Test
    @Disabled
    void verifyExpiration() {
        User user = User.builder().id(USER_ID).name("User").build();

        String refreshTokenString = "refreshtoken";

//        String instantExpected = "2014-12-22T10:15:30Z";
//        Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
//        Instant instant = Instant.now(clock);
//
//        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
//            mockedStatic.when(Instant::now).thenReturn(instant);
//            Instant now = Instant.now();
//            assertThat(now.toString()).isEqualTo(instantExpected);
//        }

        RefreshToken expectedRefreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .expiryDate(Instant.now())
                .build();

        RefreshToken actualRefreshToken = subject.verifyExpiration(expectedRefreshToken);

        assertThat(actualRefreshToken).isEqualTo(expectedRefreshToken);
    }

    @Test
    void verifyExpiration_whenTokenExpired_thenThrowsTokenRefreshException() {
        User user = User.builder().id(USER_ID).name("User").build();

        String refreshTokenString = "refreshtoken";

        RefreshToken expectedRefreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .expiryDate(Instant.MIN)
                .build();

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> subject.verifyExpiration(expectedRefreshToken));

        String expectedMessage = "Refresh token was expired. Please make a new login request";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void deleteByUserId() {
        User user = User.builder().id(USER_ID).name("User").build();

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        subject.deleteByUserId(USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);
    }
}