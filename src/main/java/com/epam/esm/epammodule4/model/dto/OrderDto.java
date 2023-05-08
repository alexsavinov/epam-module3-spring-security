package com.epam.esm.epammodule4.model.dto;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto extends RepresentationModel<OrderDto> {

    private Long id;
    private Double price;
    private UserDto user;
    private GiftCertificateDto certificate;
    private String createDate;
    private String lastUpdateDate;
}
