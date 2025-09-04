package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.service.InsurancePolicyService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.InsurancePolicyDto;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


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

    @PostMapping("/insurance")
    public ResponseEntity<InsurancePolicyDto> createInsurance(@RequestBody InsurancePolicyDto insuranceDto){
        InsurancePolicyDto i = insuranceService.create(insuranceDto);
        URI locationHeader = URI.create("/api/insurances/" + i.id());

        return ResponseEntity
                        .created(locationHeader)
                        .body(i);
    }

    @PutMapping("insurance/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InsurancePolicyDto dto) {
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
