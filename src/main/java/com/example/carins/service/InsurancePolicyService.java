package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.InsurancePolicyDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.NoSuchElementException;

@Service
public class InsurancePolicyService {

    private final InsurancePolicyRepository policyRepository;
    private final CarRepository carRepository;


    public InsurancePolicyService(CarRepository carRepository, InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
        this.carRepository = carRepository;
    }

    public List<InsurancePolicy> listInsurances() {
        return policyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public InsurancePolicyDto findById(Long id) {
        InsurancePolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Insurance policy with id " + id + " not found!"));

        return toDto(policy);
    }

    @Transactional
    public InsurancePolicyDto create(InsurancePolicyDto dto) {
        Car car = carRepository.findById(dto.carId())
                .orElseThrow(() -> new NoSuchElementException("Car with id " + dto.carId() + " not found!"));

        if (dto.startDate().isAfter(dto.endDate())) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }

        InsurancePolicy policy = new InsurancePolicy(car, dto.provider(), dto.startDate(), dto.endDate());
        return toDto(policyRepository.save(policy));
    }

    @Transactional
    public InsurancePolicyDto update(Long id, InsurancePolicyDto dto) {
        InsurancePolicy existing = policyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Insurance policy with id " + id + " not found!"));

        if (dto.startDate().isAfter(dto.endDate())) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }

        if (dto.carId() != null) {
            Car car = carRepository.findById(dto.carId())
                    .orElseThrow(() -> new NoSuchElementException("Car with id " + dto.carId() + " not found!"));
            existing.setCar(car);
        }

        existing.setProvider(dto.provider());
        existing.setStartDate(dto.startDate());
        existing.setEndDate(dto.endDate());

        return toDto(policyRepository.save(existing));
    }

    private InsurancePolicyDto toDto(InsurancePolicy p) {
        return new InsurancePolicyDto(
                p.getId(),
                p.getCar() != null ? p.getCar().getId() : null,
                p.getProvider(),
                p.getStartDate(),
                p.getEndDate()
        );
    }
}
