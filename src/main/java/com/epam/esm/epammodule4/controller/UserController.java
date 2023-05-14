package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.model.dto.UserDto;
import com.epam.esm.epammodule4.model.dto.request.CreateUserRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateUserRequest;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        userService.checkIdOfCurrentUser(id);

        User foundUser = userService.findById(id);
        UserDto userDto = modelMapper.map(foundUser, UserDto.class);

        userDto.add(linkTo(methodOn(UserController.class).getUserById(userDto.getId())).withSelfRel());
        return userDto;
    }

    @GetMapping
    public Page<UserDto> getUsers(Pageable pageable) {
        Page<User> foundUsers = userService.findAll(pageable);

        return foundUsers.map(user -> modelMapper.map(user, UserDto.class));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody CreateUserRequest createRequest) {
        User createdUser = userService.create(createRequest);
        UserDto userDto = modelMapper.map(createdUser, UserDto.class);

        userDto.add(linkTo(methodOn(UserController.class).addUser(createRequest)).withSelfRel());
        return userDto;
    }

    @PatchMapping
    public UserDto updateUser(@RequestBody UpdateUserRequest updateRequest) {
        User updatedTag = userService.update(updateRequest);
        UserDto userDto = modelMapper.map(updatedTag, UserDto.class);

        userDto.add(linkTo(methodOn(UserController.class).updateUser(updateRequest)).withSelfRel());
        return userDto;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable Long id) {
        userService.delete(id);
    }
}
