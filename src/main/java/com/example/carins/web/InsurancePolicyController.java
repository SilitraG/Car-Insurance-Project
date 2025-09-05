package com.example.carins.web;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.service.InsurancePolicyService;
import com.example.carins.web.dto.InsurancePolicyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InsurancePolicyController {

    private final InsurancePolicyService insuranceService;

    public InsurancePolicyController(InsurancePolicyService service) {
        this.insuranceService = service;
    }

    @GetMapping("/insurances")
    public List<InsurancePolicyDto> getInsurances() {
        return insuranceService.listInsurances().stream().map(this::toDto).toList();
    }

    @GetMapping("/insurances/{id}")
    public ResponseEntity<InsurancePolicyDto> getInsurance(@PathVariable Long id) {
        return ResponseEntity.ok(insuranceService.findById(id));
    }

    @PostMapping("/insurances")
    public ResponseEntity<InsurancePolicyDto> createInsurance(@Valid @RequestBody InsurancePolicyDto insuranceDto){
        InsurancePolicyDto i = insuranceService.create(insuranceDto);
        URI locationHeader = URI.create("/api/insurances/" + i.id());

        return ResponseEntity
                        .created(locationHeader)
                        .body(i);
    }

    @PutMapping("insurances/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody InsurancePolicyDto dto) {
            return ResponseEntity.ok(insuranceService.update(id, dto));
    }

    private InsurancePolicyDto toDto(InsurancePolicy i) {
        var c = i.getCar();
        return new InsurancePolicyDto(
                i.getId(),
                c != null ? c.getId() : null,
                i.getProvider(),
                i.getStartDate(),
                i.getEndDate());
    }
}
