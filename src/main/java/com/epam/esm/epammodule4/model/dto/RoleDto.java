package com.epam.esm.epammodule4.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto extends RepresentationModel<RoleDto> {

    private Long id;
    private String name;
}
