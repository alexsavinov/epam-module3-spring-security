package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.exception.UserNotFoundException;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.PageableUserRepository;
import com.epam.esm.epammodule4.repository.UserRepository;
import com.epam.esm.epammodule4.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 1L;
    @InjectMocks
    private UserServiceImpl subject;
    @Mock
    private UserRepository userRepository;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;
    @Mock
    UserDetails principal;
    @Mock
    private PageableUserRepository pageableUserRepository;

    @Test
    void findById() {
        User expectedUser = User.builder().id(USER_ID).build();
//        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUsername()).thenReturn("user1");
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(expectedUser));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(expectedUser));

        SecurityContextHolder.setContext(securityContext);

        User actualUser = subject.findById(USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void findById_whenUserIsNotFoundById_throwsUserNotFoundException() {
        String username = "user1";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUsername()).thenReturn(username);
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        SecurityContextHolder.setContext(securityContext);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> subject.findById(USER_ID));

        verifyNoMoreInteractions(userRepository);

        String expectedMessage = "Requested resource not found (username = user1)";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findAll() {
        List<User> expectedUsers = List.of(new User());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<User> pageableExpectedUsers = new PageImpl(expectedUsers, pageable, expectedUsers.size());

        when(pageableUserRepository.findAll(any(Pageable.class))).thenReturn(pageableExpectedUsers);

        Page<User> actualUsers = subject.findAll(pageable);

        verify(pageableUserRepository).findAll(pageable);
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUsers).isEqualTo(pageableExpectedUsers);
    }

    @Test
    void findByName() {
        User expectedUser = new User();

        when(userRepository.findFirstByName(any(String.class))).thenReturn(Optional.of(expectedUser));

        User actualUser = subject.findByName("tag1");

        verify(userRepository).findFirstByName(any(String.class));
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void findByName_whenUserWithNameNotExists_throwsUserNotFoundException() {
        User searchUser = User.builder().id(USER_ID).name("myUser").build();

        when(userRepository.findFirstByName(any(String.class))).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> subject.findByName(searchUser.getName()));

        verify(userRepository).findFirstByName(any(String.class));
        verifyNoMoreInteractions(userRepository);

        String expectedMessage = "Requested resource not found (name = %s)".formatted(searchUser.getName());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }
}