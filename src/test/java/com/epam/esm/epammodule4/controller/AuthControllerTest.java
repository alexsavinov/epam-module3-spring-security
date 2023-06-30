package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.controller.advice.ApplicationControllerAdvice;
import com.epam.esm.epammodule4.model.dto.request.*;
import com.epam.esm.epammodule4.model.dto.response.JwtResponse;
import com.epam.esm.epammodule4.model.dto.response.MessageResponse;
import com.epam.esm.epammodule4.model.entity.*;
import com.epam.esm.epammodule4.security.jwt.JwtUtils;
import com.epam.esm.epammodule4.service.UserService;
import com.epam.esm.epammodule4.service.implementation.RefreshTokenService;
import com.epam.esm.epammodule4.service.implementation.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final Long USER_ID = 1L;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtUtils jwtUtils;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private AuthController subject;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subject)
                .setControllerAdvice(new ApplicationControllerAdvice())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void authenticateUser() throws Exception {
        Collection<? extends GrantedAuthority> roles = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        String accessTokenString = "accessToken1234567890abcdef";
        String refreshTokenString = "refreshToken1234567890abcdef";


        UserDetailsImpl userDetails = new UserDetailsImpl(
                USER_ID, "user", "user@mail.com", "pass", roles
        );

        Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return roles;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return userDetails;
            }

            @Override
            public Object getPrincipal() {
                return userDetails;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return null;
            }
        };

        RefreshToken refreshToken = RefreshToken.builder().token(refreshTokenString).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(refreshTokenService.createRefreshToken(any(Long.class)))
                .thenReturn(refreshToken);

        JwtResponse jwtResponse = new JwtResponse(
                accessTokenString,
                refreshTokenString,
                USER_ID,
                "user",
                "user@mail.com",
                null);

        RequestBuilder requestBuilder = post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(jwtResponse))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jwtResponse.getId().toString()))
                .andExpect(jsonPath("$.refreshToken").value(jwtResponse.getRefreshToken().toString()))
                .andExpect(jsonPath("$.username").value(jwtResponse.getUsername().toString()))
                .andExpect(jsonPath("$.email").value(jwtResponse.getEmail().toString()));

        verifyNoMoreInteractions(authenticationManager, refreshTokenService);
    }

    @Test
    void registerUser() throws Exception {
        MessageResponse messageResponse = new MessageResponse("User registered successfully!");

        SignupRequest signupRequest = SignupRequest.builder()
                .name("user")
                .username("user")
                .email("user@mail.com")
                .password("password")
                .role(Set.of("admin", "user"))
                .build();

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username("user")
                .email("user@mail.com")
                .build();

        when(userService.existsByUsername(any(String.class))).thenReturn(false);
        when(userService.existsByEmail(any(String.class))).thenReturn(false);
        when(modelMapper.map(any(SignupRequest.class), any(Class.class))).thenReturn(createUserRequest);

        RequestBuilder requestBuilder = post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(signupRequest))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(messageResponse.getMessage()));

        verify(userService).existsByUsername("user");
        verify(userService).existsByEmail("user@mail.com");
        verify(userService).create(any(CreateUserRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void registerUser_whenNotExistsByUsername_thenErrorMessage() throws Exception {
        MessageResponse messageResponse = new MessageResponse("Error: Username is already taken!");

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username("user")
                .email("user@mail.com")
                .build();

        when(userService.existsByUsername(any(String.class))).thenReturn(true);

        RequestBuilder requestBuilder = post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createUserRequest))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(messageResponse.getMessage()));

        verify(userService).existsByUsername("user");
        verifyNoMoreInteractions(userService);
    }

    @Test
    void registerUser_whenNotExistsByEmail_thenErrorMessage() throws Exception {
        MessageResponse messageResponse = new MessageResponse("Error: Email is already in use!");

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username("user")
                .email("user@mail.com")
                .build();

        when(userService.existsByUsername(any(String.class))).thenReturn(false);
        when(userService.existsByEmail(any(String.class))).thenReturn(true);

        RequestBuilder requestBuilder = post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createUserRequest))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(messageResponse.getMessage()));

        verify(userService).existsByUsername("user");
        verify(userService).existsByEmail("user@mail.com");
        verifyNoMoreInteractions(userService);
    }

    @Test
    void refreshtoken() throws Exception {
        String refreshTokenString = "12345";
        TokenRefreshRequest request = new TokenRefreshRequest(refreshTokenString);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(User.builder().id(USER_ID).name("User").build())
                .build();

        when(refreshTokenService.findByToken(any(String.class))).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(refreshToken);

        RequestBuilder requestBuilder = post("/auth/refreshtoken")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value(refreshTokenString));

        verify(refreshTokenService).findByToken(refreshTokenString);
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(jwtUtils).generateTokenFromUser(null);
        verifyNoMoreInteractions(refreshTokenService, jwtUtils);
    }

    @Test
    void refreshtoken_whenAccessTokenNotFound_themThrowTokenRefreshException() throws Exception {
        String refreshToken = "12345";
        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

        RequestBuilder requestBuilder = post("/auth/refreshtoken")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict());
    }


    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk());
    }
}