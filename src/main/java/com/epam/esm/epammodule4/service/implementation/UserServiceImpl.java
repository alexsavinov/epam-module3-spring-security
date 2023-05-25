package com.epam.esm.epammodule4.service.implementation;

import com.epam.esm.epammodule4.exception.*;
import com.epam.esm.epammodule4.model.ERole;
import com.epam.esm.epammodule4.model.dto.request.CreateUserRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateUserRequest;
import com.epam.esm.epammodule4.model.entity.Order;
import com.epam.esm.epammodule4.model.entity.Role;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.OrderRepository;
import com.epam.esm.epammodule4.repository.PageableUserRepository;
import com.epam.esm.epammodule4.repository.RoleRepository;
import com.epam.esm.epammodule4.repository.UserRepository;
import com.epam.esm.epammodule4.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Optional.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PageableUserRepository pageableUserRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findById(Long id) {
        log.debug("Looking for a user with id {}", id);

        checkIdOfCurrentUser(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Requested resource not found (id = %s)"
                        .formatted(id)
                ));

        log.info("Retrieved a user with id {}", id);
        return user;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        log.debug("Retrieving users. Page request: {}", pageable);

        Page<User> users = pageableUserRepository.findAll(pageable);

        log.info("Retrieved {} users of {} total", users.getSize(), users.getTotalElements());
        return users;
    }

    @Override
    public User findByName(String name) {
        log.debug("Looking for a user with name {}", name);
        Optional<User> user = userRepository.findFirstByName(name);

        user.orElseThrow(() -> new UserNotFoundException(
                "Requested resource not found (name = %s)".formatted(name)
        ));

        log.info("Found a user with name {}", name);
        return user.get();
    }

    @Override
    public User findByUsername(String username) {
        log.debug("Looking for a user with username {}", username);
        Optional<User> user = userRepository.findByUsername(username);

        user.orElseThrow(() -> new UserNotFoundException(
                "Requested resource not found (username = %s)".formatted(username)
        ));

        log.info("Found a user with username {}", username);
        return user.get();
    }

    @Override
    public User findByEmail(String email) {
        log.debug("Looking for a user with email {}", email);
        Optional<User> user = userRepository.findByEmail(email);

        user.orElseThrow(() -> new UserNotFoundException(
                "Requested resource not found (email = %s)".formatted(email)
        ));

        log.info("Found a user with email {}", email);
        return user.get();
    }

    @Override
    public User create(CreateUserRequest createRequest) {
        log.debug("Creating a new user");

        User newUser = User.builder()
                .name(createRequest.getName())
                .username(createRequest.getUsername())
                .email(createRequest.getEmail())
                .password(passwordEncoder.encode(createRequest.getPassword()))
                .roles(parseRoles(createRequest.getRole()))
                .build();

        try {
            User createdUser = userRepository.save(newUser);

            log.info("Created a new user with id {}", createdUser.getId());
            return createdUser;
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException(
                    "Requested resource already exists (username = %s)".formatted(createRequest.getUsername()));
        }
    }

    @Override
    public User update(UpdateUserRequest updateRequest) {
        log.debug("Updating user");

        checkIdOfCurrentUser(updateRequest.getId());

        User user = User.builder()
                .id(updateRequest.getId())
                .name(updateRequest.getName())
                .username(updateRequest.getUsername())
                .email(updateRequest.getEmail())
                .password(passwordEncoder.encode(updateRequest.getPassword()))
                .roles(parseRoles(updateRequest.getRole()))
                .build();

        try {
            User updatedUser = userRepository.save(user);

            log.info("Updated a user with id {}", updatedUser.getId());
            return updatedUser;
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException(
                    "Requested resource already exists (username = %s)".formatted(updateRequest.getUsername()));
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting user with id {}", id);

        User foundUser = findById(id);

        Pageable pageable = PageRequest.of(0, 1);
        Page<Order> foundOrders = orderRepository.findAllByUserId(id, pageable);

        if (foundOrders.getTotalElements() > 0) {
            throw new UserCannotDeleteException(
                    "User cannot be deleted - has found in (%d) orders".formatted(foundOrders.getTotalElements()));
        }

        userRepository.delete(foundUser);

        log.info("User with id {} is deleted", foundUser.getId());
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Map<String, Object> getUserClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser principal = ((OidcUser) authentication.getPrincipal());
            return principal.getClaims();
        }

        return Collections.emptyMap();
    }

    public void checkIdOfCurrentUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Object principal = authentication.getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        User user = findByUsername(username);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean roleAdmin = authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!roleAdmin && user.getId() != id) {
            throw new UserIdIncorrectException(
                    "User id (%d) belongs to another user".formatted(id)
            );
        }
    }

    private Set<Role> parseRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();

        if (ofNullable(strRoles).isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RoleNotFoundException("Error: Role USER is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                if (role.equals("admin")) {
                    throw new RoleNotFoundException("Role ADMIN cannot be assigned on registration.");
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RoleNotFoundException("Role USER is not found."));
                    roles.add(userRole);
                }
            });
        }

        return roles;
    }
}
