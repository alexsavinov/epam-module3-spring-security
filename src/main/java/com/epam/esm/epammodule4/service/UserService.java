package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.dto.request.CreateUserRequest;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.model.entity.User;

import java.util.Map;

public interface UserService extends PageableUserService {

    User findById(Long id);

    User findByName(String name);

    User findByUsername(String name);

    User findByEmail(String name);

    User create(CreateUserRequest createRequest);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Map<String, Object> getUserClaims();

//    User addRoleToUser(String username, String roleName);
}
