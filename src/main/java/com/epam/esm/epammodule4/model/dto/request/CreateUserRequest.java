package com.epam.esm.epammodule4.model.dto.request;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {

    private String name;
    private String email;
    private String username;
    private String password;
    private Set<String> role;
}
