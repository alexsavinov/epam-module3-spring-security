package com.epam.esm.epammodule4.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGiftCertificateRequest {

    private String name;
    private String description;
    private Double price;
    private Integer duration;
    private List<CreateTagRequest> tags;
}
