package com.epam.esm.epammodule4.service.implementation;

import com.epam.esm.epammodule4.exception.UserAlreadyExistsException;
import com.epam.esm.epammodule4.exception.UserIdIncorrectException;
import com.epam.esm.epammodule4.exception.UserNotFoundException;
import com.epam.esm.epammodule4.model.ERole;
import com.epam.esm.epammodule4.model.dto.request.CreateUserRequest;
import com.epam.esm.epammodule4.model.entity.Role;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.PageableUserRepository;
import com.epam.esm.epammodule4.repository.RoleRepository;
import com.epam.esm.epammodule4.repository.UserRepository;
import com.epam.esm.epammodule4.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PageableUserRepository pageableUserRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findById(Long id) {
        log.debug("Looking for a user with id {}", id);

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

        Set<String> strRoles = createRequest.getRole();
        Set<Role> roles = new HashSet<>();

        System.out.println("** strRoles {%s}".formatted(strRoles));
        System.out.println("** signUpRequest.getRole() {%s}".formatted(createRequest.getRole()));

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        throw new RuntimeException("Error: Role ADMIN cannot be added.");
//                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                        roles.add(adminRole);
//
//                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }


        User newUser = User.builder()
                .name(createRequest.getName())
                .username(createRequest.getUsername())
                .email(createRequest.getEmail())
                .password(passwordEncoder.encode(createRequest.getPassword()))
                .roles(roles)
                .build();

        System.out.println("###### newUser = {%s} {%s} {%s} {%s} {%s}"
                .formatted(newUser.getName(), newUser.getEmail(),
                        newUser.getUsername(), newUser.getPassword(), newUser.getRoles()));
//        User newUser = modelMapper.map(createRequest, User.class);
        User createdUser;

        try {
            createdUser = userRepository.save(newUser);

            log.info("Created a new user with id {}", createdUser.getId());
            return createdUser;
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException(
                    "Requested resource already exists (username = %s)".formatted(createRequest.getUsername()));
        }
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
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser principal = ((OidcUser) authentication.getPrincipal());
            return principal.getClaims();
        }
        return Collections.emptyMap();
    }

    public void checkIdOfCurrentUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean roleAdmin = authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        String username = ((UserDetails) principal).getUsername();
        User user = findByUsername(username);

        if (!roleAdmin && !user.getId().equals(id)) {
            throw new UserIdIncorrectException(
                    "User id (%d) belongs to another user".formatted(id)
            );
        }
    }
}
