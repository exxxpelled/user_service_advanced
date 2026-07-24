package com.innowise.userservice.paymentcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreatePaymentCardRequest(

        @NotNull
        Long userId,

        @NotBlank
        @Pattern(regexp = "^[0-9]{16}$")
        String number
) {
}