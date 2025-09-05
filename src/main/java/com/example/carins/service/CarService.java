package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.ClaimDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Claim> history(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new NoSuchElementException("Car with id " + carId + " not found!");
        }

        return claimRepository.findClaimsForCar(carId);
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;

        if (!carRepository.existsById(carId)) {
            throw new NoSuchElementException("Car with id " + carId + " not found!");
        }
        return policyRepository.existsActiveOnDate(carId, date);
    }

    @Transactional(readOnly = true)
    public ClaimDto findById(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Claim with id " + id + " not found!"));

        return toDto(claim);
    }

    @Transactional
    public ClaimDto create(ClaimDto dto) {
        Car car = carRepository.findById(dto.carId())
                .orElseThrow(() -> new NoSuchElementException("Car with id " + dto.carId() + " not found!"));

        Claim claim = new Claim(car, dto.claimDate(), dto.description(), dto.amount());
        return toDto(claimRepository.save(claim));
    }

    private ClaimDto toDto(Claim c) {
        return new ClaimDto(
                c.getId(),
                c.getCar() != null ? c.getCar().getId() : null,
                c.getClaimDate(),
                c.getDescription(),
                c.getAmount()
        );
    }
}
