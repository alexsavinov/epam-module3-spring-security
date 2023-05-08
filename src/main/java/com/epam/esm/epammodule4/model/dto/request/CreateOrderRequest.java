package com.epam.esm.epammodule4.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    private Long userId;
    private Long certificateId;
}
