package com.epam.esm.epammodule4.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    private String name;
    private String username;
    private String email;
    private String password;
    private Set<String> role;
}
