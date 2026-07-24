package com.innowise.userservice.user.dto;

import com.innowise.userservice.paymentcard.dto.PaymentCardResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;

public record UserResponse(

        Long id,

        @NotBlank
        String name,

        @NotBlank
        String surname,

        @NotNull
        @PastOrPresent
        LocalDate birthDate,

        @NotBlank
        @Email
        String email,

        @NotNull
        Boolean active,

        List<PaymentCardResponse> cards
) {
}