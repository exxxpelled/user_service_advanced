package com.innowise.userservice.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record CreateUserRequest(

        @NotBlank
        String name,

        @NotBlank
        String surname,

        @NotNull
        @Past
        LocalDate birthDate,

        @NotBlank
        @Email
        String email
) {
}