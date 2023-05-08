package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.model.dto.RoleDto;
import com.epam.esm.epammodule4.model.dto.request.CreateRoleRequest;
import com.epam.esm.epammodule4.model.dto.request.DeleteRoleRequest;
import com.epam.esm.epammodule4.model.entity.Role;
import com.epam.esm.epammodule4.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/roles")
public class RoleController {

//    private final RoleService roleService;
//    private final ModelMapper modelMapper;

//    @Secured({"USER"})
    @GetMapping
    public Page<RoleDto> getRoles(Pageable pageable) {
//        Page<Role> foundRoles = roleService.findAll(pageable);
//
//        return foundRoles.map(role -> modelMapper.map(role, RoleDto.class));
        return null;
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @Secured({"ADMIN"})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDto addRole(@RequestBody CreateRoleRequest createRequest) {
//        Role createdRole = roleService.create(createRequest);
//        RoleDto roleDto = modelMapper.map(createdRole, RoleDto.class);
//
//        roleDto.add(linkTo(methodOn(RoleController.class).addRole(createRequest)).withSelfRel());
//        return roleDto;
        return null;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoleByName(@RequestBody DeleteRoleRequest deleteRequest) {
//        roleService.delete(deleteRequest.getName());
    }
}
