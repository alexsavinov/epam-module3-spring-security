package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableUserService {

    Page<User> findAll(Pageable pageable);
}
