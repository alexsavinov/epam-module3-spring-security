package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.dto.request.CreateRoleRequest;
import com.epam.esm.epammodule4.model.entity.Role;

public interface RoleService extends PageableRoleService {

    Role findByName(String name);

    Role create(CreateRoleRequest createRoleRequest);

    void delete(String name);
}
