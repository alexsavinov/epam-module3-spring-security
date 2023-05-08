package com.epam.esm.epammodule4.model.dto;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiftCertificateDto extends RepresentationModel<GiftCertificateDto> {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    private List<TagDto> tags;
    private String createDate;
    private String lastUpdateDate;
}
