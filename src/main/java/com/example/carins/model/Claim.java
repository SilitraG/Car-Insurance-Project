package com.example.carins.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "claim")
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Car car;

    private LocalDate claimDate;
    private String description;
    private BigDecimal amount;

    public Claim() {}

    public Claim(Car car, LocalDate claimDate, String description, BigDecimal amount) {
        this.car = car;
        this.claimDate = claimDate;
        this.description = description;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}