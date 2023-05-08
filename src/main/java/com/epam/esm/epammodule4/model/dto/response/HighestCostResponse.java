package com.epam.esm.epammodule4.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HighestCostResponse extends RepresentationModel<HighestCostResponse> {

    private Double highestCost;
}
