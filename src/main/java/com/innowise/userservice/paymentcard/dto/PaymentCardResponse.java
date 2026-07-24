package com.innowise.userservice.paymentcard.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;

public record PaymentCardResponse(

        Long id,

        @NotNull
        Long userId,

        @NotBlank
        String number,

        @Null
        String holder,

        @NotNull
        @FutureOrPresent
        LocalDate expirationDate,

        @NotNull
        Boolean active
) {
}