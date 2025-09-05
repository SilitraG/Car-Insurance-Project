package com.example.carins.repo;

import com.example.carins.model.PolicyExpiryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyExpiryLogRepository extends JpaRepository<PolicyExpiryLog, Long> {
    boolean existsByPolicyId(Long policyId);
}
