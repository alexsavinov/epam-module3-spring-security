package com.epam.esm.epammodule4.repository;

import com.epam.esm.epammodule4.model.ERole;
import com.epam.esm.epammodule4.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}
