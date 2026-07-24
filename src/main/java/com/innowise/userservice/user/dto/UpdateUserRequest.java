package com.innowise.userservice.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UpdateUserRequest(

        String name,

        String surname,

        @Past
        LocalDate birthDate,

        @Email
        String email
) {
}