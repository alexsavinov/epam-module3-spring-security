package com.epam.esm.epammodule4.model.dto;

import com.epam.esm.epammodule4.model.entity.Role;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Column;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends RepresentationModel<UserDto> {

    private Long id;
    private String email;
    private String name;
    private String username;
    private String password;
    private Set<Role> roles;
}
