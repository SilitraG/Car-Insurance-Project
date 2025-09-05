package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.ClaimDto;
import com.example.carins.web.dto.InsurancePolicyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toCarDto).toList();
    }

    @GetMapping("/cars/{carId}/history")
    public List<ClaimDto> history(@PathVariable Long carId) {
        return service.history(carId).stream().map(this::toClaimDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        // TODO: validate date format and handle errors consistently
        LocalDate d = LocalDate.parse(date);
        boolean valid = service.isInsuranceValid(carId, d);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));
    }

    @GetMapping("/claims/{id}")
    public ResponseEntity<ClaimDto> getInsurance(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClaimDto> create(@PathVariable Long carId, @Valid @RequestBody ClaimDto body) {
        ClaimDto dto = new ClaimDto(null, carId, body.claimDate(), body.description(), body.amount());
        return ResponseEntity.status(201).body(service.create(dto));
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
