package com.example.carins.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ClaimDto(
        Long id,
        @NotNull Long carId,

        @NotNull(message = "Claim date must not be null!")
        @PastOrPresent(message = "Claim date cannot be in the future!")
        LocalDate claimDate,

        @NotBlank(message = "Description must not be blank!")
        @Size(max = 500, message = "Description must not exceed 500 characters!")
        String description,

        @NotNull(message = "Amount must not be null!")
        @Positive(message = "Amount must not be negative!")
        @Digits(integer = 6, fraction = 2, message = "Amount must be lower than 1.000.000!")
        BigDecimal amount
) {}