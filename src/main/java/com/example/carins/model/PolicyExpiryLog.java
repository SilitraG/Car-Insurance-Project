package com.example.carins.model;

import jakarta.persistence.*;

@Entity
@Table(name = "policy_expiry_log",
        uniqueConstraints = @UniqueConstraint(columnNames = "policy_id"))
public class PolicyExpiryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false, unique = true)
    private InsurancePolicy policy;

    public PolicyExpiryLog() {}

    public PolicyExpiryLog(InsurancePolicy policy) {
        this.policy = policy;
    }

    public Long getId() { return id; }
    public InsurancePolicy getPolicy() { return policy; }
    public void setPolicy(InsurancePolicy policy) { this.policy = policy; }
}
