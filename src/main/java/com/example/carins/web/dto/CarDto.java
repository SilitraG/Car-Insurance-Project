package com.example.carins.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarDto(
        Long id,

        @NotBlank(message = "Vehicle identification number must not be blank!")
        @Size(min = 8, max = 8, message = "VIN must have exactly 8 characters")
        String vin,
        String make,
        String model,
        int year,
        Long ownerId,
        String ownerName,
        String ownerEmail
) {}
