package com.example.carins.service;

import com.example.carins.model.Owner;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.web.dto.OwnerDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }


    @Transactional(readOnly = true)
    public OwnerDto findCarById(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Owner with id " + id + " not found!"));

        return ownerToDto(owner);
    }

    @Transactional
    public OwnerDto create(OwnerDto dto) {
        String email = dto.email().trim().toLowerCase();
        if (ownerRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner with email " + email + " already exists!");
        }

        Owner o = new Owner(dto.name(), email);
        return ownerToDto(ownerRepository.save(o));
    }

    @Transactional
    public OwnerDto update(Long id, OwnerDto dto) {
        Owner existing = ownerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Owner with id " + id + " not found!"));

        String email = dto.email().trim().toLowerCase();
        if (ownerRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner with email " + email + " already exists!");
        }

        existing.setName(dto.name());
        existing.setEmail(email);

        return ownerToDto(ownerRepository.save(existing));
    }

    private OwnerDto ownerToDto(Owner o) {
        return new OwnerDto(o.getId(), o.getName(), o.getEmail());
    }
}
