package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.dto.request.CreateUserRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateUserRequest;
import com.epam.esm.epammodule4.model.entity.User;

public interface UserService extends PageableUserService {

    User findById(Long id);

    User findByName(String name);

    User findByUsername(String name);

    User findByEmail(String name);

    User create(CreateUserRequest createRequest);

    User update(UpdateUserRequest updateRequest);

    void delete(Long id);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    void checkIdOfCurrentUser(Long id);
}
