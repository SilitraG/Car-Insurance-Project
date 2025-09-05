package com.example.carins.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OwnerDto(
        Long id,

        @NotBlank(message = "Owner name must not be blank!")
        String name,

        @NotBlank(message = "Owner email must not be blank!")
        @Email(message = "Owner email must be a valid email address!")
        String email
) {}
