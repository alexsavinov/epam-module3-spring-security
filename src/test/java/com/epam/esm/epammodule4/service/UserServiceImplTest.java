package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.exception.UserCannotDeleteException;
import com.epam.esm.epammodule4.exception.UserNotFoundException;
import com.epam.esm.epammodule4.model.ERole;
import com.epam.esm.epammodule4.model.dto.request.CreateUserRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateUserRequest;
import com.epam.esm.epammodule4.model.entity.*;
import com.epam.esm.epammodule4.repository.OrderRepository;
import com.epam.esm.epammodule4.repository.PageableUserRepository;
import com.epam.esm.epammodule4.repository.RoleRepository;
import com.epam.esm.epammodule4.repository.UserRepository;
import com.epam.esm.epammodule4.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long CERTIFICATE_ID = 1L;
    private static final Long ORDER_ID = 1L;
    @InjectMocks
    private UserServiceImpl subject;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;
    @Mock
    UserDetails principal;
    @Mock
    private PageableUserRepository pageableUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;;
    @Mock
    private RoleRepository roleRepository;;

    @Test
    void findById() {
        User expectedUser = User.builder().id(USER_ID).build();

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

        User actualUser = subject.findByName("user1");

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

    @Test
    void findByUsername() {
        User expectedUser = new User();

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(expectedUser));

        User actualUser = subject.findByUsername("user1");

        verify(userRepository).findByUsername(any(String.class));
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void findByUsername_whenUserWithNameNotExists_throwsUserNotFoundException() {
        User searchUser = User.builder().id(USER_ID).name("myUser").build();

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> subject.findByUsername(searchUser.getName()));

        verify(userRepository).findByUsername(any(String.class));
        verifyNoMoreInteractions(userRepository);

        String expectedMessage = "Requested resource not found (username = %s)".formatted(searchUser.getName());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findByEmail() {
        User expectedUser = new User();

        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(expectedUser));

        User actualUser = subject.findByEmail("user1@mail.com");

        verify(userRepository).findByEmail(any(String.class));
        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void findByEmail_whenUserWithNameNotExists_throwsUserNotFoundException() {
        User searchUser = User.builder().id(USER_ID).name("myUser").build();

        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> subject.findByEmail(searchUser.getName()));

        verify(userRepository).findByEmail(any(String.class));
        verifyNoMoreInteractions(userRepository);

        String expectedMessage = "Requested resource not found (email = %s)".formatted(searchUser.getName());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void create() {
        CreateUserRequest createRequest = CreateUserRequest.builder().name("myUser").password("pass").build();

        User expectedUser = User.builder().id(USER_ID).name("myUser").build();

        Role role = Role.builder().name(ERole.ROLE_USER).build();

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(roleRepository.findByName(any(ERole.class))).thenReturn(Optional.of(role));

        User actualUser = subject.create(createRequest);

        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void update() {
        Role role = Role.builder().name(ERole.ROLE_USER).build();
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .id(USER_ID)
                .name("myUser")
                .password("pass")
                .role(Set.of("ROLE_USER"))
                .build();

        User expectedUser = User.builder().id(USER_ID).name("myUser").password("pass").build();


        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(roleRepository.findByName(any(ERole.class))).thenReturn(Optional.of(role));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUsername()).thenReturn("user1");
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(expectedUser));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(expectedUser));

        SecurityContextHolder.setContext(securityContext);

        User actualUser = subject.update(updateRequest);

        verifyNoMoreInteractions(userRepository);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void delete() {
        User deleteUser = User.builder().id(USER_ID).name("myUser").build();

        List<Order> expectedOrders = new ArrayList<>();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> pageableExpectedOrders = new PageImpl(expectedOrders, pageable, expectedOrders.size());

        when(orderRepository.findAllByUserId(any(Long.class), any(Pageable.class))).thenReturn(pageableExpectedOrders);


        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(deleteUser));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUsername()).thenReturn("user1");
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(deleteUser));

        SecurityContextHolder.setContext(securityContext);

        subject.delete(USER_ID);

        verify(userRepository).delete(deleteUser);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_whenUserOrdersFound_thenThrowsUserCannotDeleteException() {
        User deleteUser = User.builder().id(USER_ID).name("myUser").build();

        GiftCertificate certificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("cert")
                .build();

        Order expectedOrder = Order.builder()
                .id(ORDER_ID)
                .price(10.2)
                .user(deleteUser)
                .giftCertificate(certificate)
                .build();

        List<Order> expectedOrders = List.of(expectedOrder);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> pageableExpectedOrders = new PageImpl(expectedOrders, pageable, expectedOrders.size());

        when(orderRepository.findAllByUserId(any(Long.class), any(Pageable.class))).thenReturn(pageableExpectedOrders);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(deleteUser));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUsername()).thenReturn("user1");
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(deleteUser));

        SecurityContextHolder.setContext(securityContext);

        UserCannotDeleteException exception = assertThrows(UserCannotDeleteException.class,
                () -> subject.delete(USER_ID));

        String expectedMessage = "User cannot be deleted - has found in (%d) orders"
                .formatted(pageableExpectedOrders.getTotalElements());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void existsByUsername() {
        String username = "user";

        when(userRepository.existsByUsername(any(String.class))).thenReturn(true);

        Boolean expectedResult = subject.existsByUsername(username);

        verify(userRepository).existsByUsername(any(String.class));
        assertThat(expectedResult).isEqualTo(true);
    }

    @Test
    void existsByEmail() {
        String email = "user@mail.com";

        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);

        Boolean expectedResult = subject.existsByEmail(email);

        verify(userRepository).existsByEmail(any(String.class));
        assertThat(expectedResult).isEqualTo(true);
    }
}