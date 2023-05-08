package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableRoleService {

    Page<Role> findAll(Pageable pageable);
}
