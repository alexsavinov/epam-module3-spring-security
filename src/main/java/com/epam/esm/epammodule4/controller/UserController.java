package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.model.dto.UserDto;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserDto getUserById(@PathVariable Long id) {
        userService.checkIdOfCurrentUser(id);

        User foundUser = userService.findById(id);
        UserDto userDto = modelMapper.map(foundUser, UserDto.class);

        userDto.add(linkTo(methodOn(UserController.class).getUserById(userDto.getId())).withSelfRel());
        return userDto;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> getUsers(Pageable pageable) {
        Page<User> foundUsers = userService.findAll(pageable);

        return foundUsers.map(user -> modelMapper.map(user, UserDto.class));
    }
}
