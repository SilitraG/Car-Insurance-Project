package com.example.carins.web;

import com.example.carins.service.OwnerService;
import com.example.carins.web.dto.OwnerDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class OwnerController {

    private final OwnerService service;

    public OwnerController(OwnerService service) {
        this.service = service;
    }

    @GetMapping("/owners/{id}")
    public ResponseEntity<OwnerDto> getOwner(@PathVariable Long id) {
        return ResponseEntity.ok(service.findCarById(id));
    }

    @PostMapping("/owners")
    public ResponseEntity<OwnerDto> create(@Valid @RequestBody OwnerDto dto) {
        OwnerDto o = service.create(dto);
        URI locationHeader = URI.create("/api/owners/" + o.id());

        return ResponseEntity
                .created(locationHeader)
                .body(o);
    }

    @PutMapping("/owners/{id}")
    public ResponseEntity<OwnerDto> update(@PathVariable Long id, @Valid @RequestBody OwnerDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }


}
