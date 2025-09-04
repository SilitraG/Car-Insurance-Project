package com.example.carins.web.dto;

import java.time.LocalDate;

public record InsurancePolicyDto(
        Long id,
        Long carId,
        String provider,
        LocalDate startDate,
        LocalDate endDate
){}