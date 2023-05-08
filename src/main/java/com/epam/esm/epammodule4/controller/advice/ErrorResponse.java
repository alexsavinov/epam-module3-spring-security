package com.epam.esm.epammodule4.controller.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
class ErrorResponse {

    private final String errorMessage;
    private final int errorCode;
}
