package com.epam.esm.epammodule4.model.dto;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Column;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends RepresentationModel<UserDto> {

    private Long id;
    private String name;
    private String username;
    private String password;
}
