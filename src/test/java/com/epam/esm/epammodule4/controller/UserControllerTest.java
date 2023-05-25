package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.controller.advice.ApplicationControllerAdvice;
import com.epam.esm.epammodule4.exception.TagAlreadyExistsException;
import com.epam.esm.epammodule4.exception.UserAlreadyExistsException;
import com.epam.esm.epammodule4.exception.UserCannotDeleteException;
import com.epam.esm.epammodule4.exception.UserNotFoundException;
import com.epam.esm.epammodule4.model.dto.*;
import com.epam.esm.epammodule4.model.dto.request.CreateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.CreateUserRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateUserRequest;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final Long USER_ID = 1L;
    @InjectMocks
    private UserController subject;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
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
    void getUserById() throws Exception {
        User expectedUser = new User();
        UserDto userDto = UserDto.builder().id(USER_ID).name("User1").build();

        when(userService.findById(any(Long.class))).thenReturn(expectedUser);
        when(modelMapper.map(any(User.class), any(Class.class))).thenReturn(userDto);

        mockMvc.perform(
                        get("/users/{id}", USER_ID)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(userDto.getName()));

        verify(userService).checkIdOfCurrentUser(USER_ID);
        verify(userService).findById(USER_ID);
        verify(modelMapper).map(expectedUser, UserDto.class);
        verifyNoMoreInteractions(userService, modelMapper);
    }

    @Test
    void getUserById_whenUserNotFoundExceptionIsThrows_returns404() throws Exception {
        String errorMessage = "User not found";

        when(userService.findById(any(Long.class))).thenThrow(new UserNotFoundException(errorMessage));

        mockMvc.perform(
                        get("/users/{id}", USER_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));

        verify(userService).checkIdOfCurrentUser(USER_ID);
        verify(userService).findById(USER_ID);
        verifyNoInteractions(modelMapper);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUsers() throws Exception {
        User expectedUser = User.builder().id(USER_ID).name("User").build();

        List<User> expectedUsers = List.of(expectedUser);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<User> pageableExpectedUsers = new PageImpl(expectedUsers, pageable, expectedUsers.size());

        when(userService.findAll(any(Pageable.class))).thenReturn(pageableExpectedUsers);

        mockMvc.perform(
                        get("/users")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "name,asc")
                )
                .andExpect(status().isOk());

        verify(userService).findAll(pageable);
        verifyNoMoreInteractions(userService);
    }


    @Test
    void addUser() throws Exception {
        User createdUser = new User();
        UserDto userDto = UserDto.builder().id(USER_ID).name("User1").build();

        when(userService.create(any(CreateUserRequest.class))).thenReturn(createdUser);
        when(modelMapper.map(any(User.class), any(Class.class))).thenReturn(userDto);

        RequestBuilder requestBuilder = post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createdUser))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(userDto.getName()));

        verify(modelMapper).map(createdUser, UserDto.class);
        verifyNoMoreInteractions(userService, modelMapper);
    }

    @Test
    void addUser_whenUserAlreadyExistsExceptionIsThrows_returns409() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();

        String errorMessage = "User already exists";

        when(userService.create(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException(errorMessage));

        RequestBuilder requestBuilder = post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createUserRequest))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));

        verifyNoInteractions(modelMapper);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUser() throws Exception {
        User updatedUser = new User();
        UserDto userDto = UserDto.builder().id(USER_ID).name("User1").build();

        when(userService.update(any(UpdateUserRequest.class))).thenReturn(updatedUser);
        when(modelMapper.map(any(User.class), any(Class.class))).thenReturn(userDto);

        RequestBuilder requestBuilder = patch("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(updatedUser))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(userDto.getName()));

        verify(modelMapper).map(updatedUser, UserDto.class);
        verifyNoMoreInteractions(userService, modelMapper);
    }

    @Test
    void deleteUserById() throws Exception {
        RequestBuilder requestBuilder = delete("/users/{id}", USER_ID);

        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());

        verify(userService).delete(USER_ID);
        verifyNoMoreInteractions(userService);
    }
}