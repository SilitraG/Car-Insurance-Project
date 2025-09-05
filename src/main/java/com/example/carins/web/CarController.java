package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.ClaimDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    private static final LocalDate minDate = LocalDate.of(1900, 1, 1);
    private static final LocalDate maxDate = LocalDate.of(2100, 12, 31);

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toCarDto).toList();
    }

    @GetMapping("/cars/{id}")
    public ResponseEntity<CarDto> getCar(@PathVariable Long id) {
        return ResponseEntity.ok(service.findCarById(id));
    }

    @PostMapping("/cars")
    public ResponseEntity<CarDto> create(@Valid @RequestBody CarDto dto) {
        CarDto c = service.create(dto);
        URI locationHeader = URI.create("/api/cars/" + c.id());

        return ResponseEntity
                .created(locationHeader)
                .body(c);
    }

    @PutMapping("/cars/{id}")
    public CarDto update(@PathVariable Long id, @Valid @RequestBody CarDto dto) {
        return service.update(id, dto);
    }

    @GetMapping("/cars/{carId}/history")
    public List<ClaimDto> history(@PathVariable Long carId) {
        return service.history(carId).stream().map(this::toClaimDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        final LocalDate d;

        try {
            d = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use ISO YYYY-MM-DD format");
        }

        if (d.isBefore(minDate) || d.isAfter(maxDate)) {
            throw new IllegalArgumentException("Date out of supported range [" + minDate + ", " + maxDate + "]");
        }

        boolean valid = service.isInsuranceValid(carId, d);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));
    }

    @GetMapping("/claims/{id}")
    public ResponseEntity<ClaimDto> getInsurance(@PathVariable Long id) {
        return ResponseEntity.ok(service.findClaimById(id));
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<ClaimDto> createInsurance(@Valid @RequestBody ClaimDto claimDto){
        ClaimDto c = service.create(claimDto);
        URI locationHeader = URI.create("/api/claims/" + c.id());

        return ResponseEntity
                .created(locationHeader)
                .body(c);
    }

    private CarDto toCarDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    private ClaimDto toClaimDto(Claim c) {
        var car = c.getCar();
        return new ClaimDto(
                c.getId(),
                car != null ? car.getId() : null,
                c.getClaimDate(),
                c.getDescription(),
                c.getAmount());
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
}
