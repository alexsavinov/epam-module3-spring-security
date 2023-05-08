package com.epam.esm.epammodule4.service.implementation;

import com.epam.esm.epammodule4.exception.RoleAlreadyExistsException;
import com.epam.esm.epammodule4.exception.RoleNotFoundException;
import com.epam.esm.epammodule4.model.dto.request.CreateRoleRequest;
import com.epam.esm.epammodule4.model.entity.Role;
import com.epam.esm.epammodule4.repository.RoleRepository;
import com.epam.esm.epammodule4.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//@Slf4j
//@Service
//@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

//    private final RoleRepository roleRepository;
//    private final ModelMapper modelMapper;

    @Override
    public Page<Role> findAll(Pageable pageable) {
//        log.debug("Retrieving roles. Page request: {}", pageable);
//
//        Page<Role> foundRoles = roleRepository.findAll(pageable);
//
//        log.info("Retrieved {} roles of {} total", foundRoles.getSize(), foundRoles.getTotalElements());
//        return foundRoles;
        return null;
    }

    @Override
    public Role findByName(String name) {
//        log.debug("Looking for a role with name {}", name);
//        Optional<Role> role = roleRepository.findByName(name);
//
//        role.orElseThrow(() -> new RoleNotFoundException(
//                "Requested resource not found (name = %s)".formatted(name)
//        ));
//
//        log.info("Found a role with name {}", name);
//        return role.get();
        return null;
    }

    @Override
    public Role create(CreateRoleRequest createRoleRequest) {
//        log.debug("Creating a new role");
//
//        Role newRole = modelMapper.map(createRoleRequest, Role.class);
//        Role createdRole;
//
//        try {
//            createdRole = roleRepository.save(newRole);
//
//            log.info("Created a new role with id {}", createdRole.getId());
//            return createdRole;
//        } catch (DataIntegrityViolationException ex) {
//            throw new RoleAlreadyExistsException(
//                    "Requested resource already exists (name = %s)".formatted(createRoleRequest.getName()));
//        }
        return null;
    }

    @Override
    @Transactional
    public void delete(String name) {
//        log.debug("Deleting role with name {}", name);
//
//        Role foundRole = findByName(name);
//
//        roleRepository.delete(foundRole);
//
//        log.info("Role with name {} is deleted", name);
    }
}
