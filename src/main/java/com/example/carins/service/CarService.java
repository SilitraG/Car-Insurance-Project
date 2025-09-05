package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.ClaimDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final OwnerRepository ownerRepository;

    public CarService(CarRepository carRepository,
                      InsurancePolicyRepository policyRepository,
                      ClaimRepository claimRepository,
                      OwnerRepository ownerRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.ownerRepository = ownerRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CarDto findCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Car with id " + id + " not found!"));

        return carToDto(car);
    }

    @Transactional
    public CarDto create(CarDto dto) {
        String vin = dto.vin().trim().toUpperCase();

        if (carRepository.existsByVinIgnoreCase(vin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Car with VIN " + vin + " already exists!");
        }

        Owner owner = ownerRepository.findById(dto.ownerId())
                .orElseThrow(() -> new NoSuchElementException("Owner with id " + dto.ownerId() + " not found!"));
        Car car = new Car(dto.vin(), dto.make(), dto.model(), dto.year(), owner);

        return carToDto(carRepository.save(car));
    }

    @Transactional
    public CarDto update(Long id, CarDto dto) {
        Car existing = carRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Car with id " + id + " not found!"));

        String vin = dto.vin().trim().toUpperCase();
        if (carRepository.existsByVinIgnoreCaseAndIdNot(vin, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Car with VIN " + vin + " already exists!");
        }

        Owner owner = ownerRepository.findById(dto.ownerId())
                .orElseThrow(() -> new NoSuchElementException("Owner with id " + dto.ownerId() + " not found!"));

        existing.setVin(vin);
        existing.setMake(dto.make());
        existing.setModel(dto.model());
        existing.setOwner(owner);

        return carToDto(carRepository.save(existing));
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
    public ClaimDto findClaimById(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Claim with id " + id + " not found!"));

        return claimToDto(claim);
    }

    @Transactional
    public ClaimDto create(ClaimDto dto) {
        Car car = carRepository.findById(dto.carId())
                .orElseThrow(() -> new NoSuchElementException("Car with id " + dto.carId() + " not found!"));

        Claim claim = new Claim(car, dto.claimDate(), dto.description(), dto.amount());
        return claimToDto(claimRepository.save(claim));
    }

    private ClaimDto claimToDto(Claim c) {
        return new ClaimDto(
                c.getId(),
                c.getCar() != null ? c.getCar().getId() : null,
                c.getClaimDate(),
                c.getDescription(),
                c.getAmount()
        );
    }

    private CarDto carToDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }
}
