package com.epam.esm.epammodule4.model.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateGiftCertificateRequest {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    private List<CreateTagRequest> tags;
}
