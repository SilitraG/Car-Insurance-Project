package com.example.carins;

import com.example.carins.jobs.PolicyExpiryScheduler;
import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.repo.PolicyExpiryLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PolicyExpirySchedulerTests {

    @Autowired InsurancePolicyRepository policyRepository;
    @Autowired OwnerRepository ownerRepository;
    @Autowired CarRepository carRepository;
    @Autowired PolicyExpiryLogRepository logRepository;
    @Autowired PolicyExpiryScheduler scheduler;

    @Test
    @Transactional
    void scheduler_logs_policy_once() {
        Owner owner = ownerRepository.save(new Owner("Ion Ionescu", "ion.ionescu@example.com"));
        Car car = carRepository.save(new Car("VIN12321", "Dacia", "Sandero", 2016, owner));

        InsurancePolicy policy = policyRepository.saveAndFlush(
                new InsurancePolicy(car,
                "Allianz",
                        LocalDate.now().minusYears(1),
                        LocalDate.now().minusDays(1)));

        Long policyId = policy.getId();

        scheduler.logRecentlyExpiredPolicies();
        assertTrue(logRepository.existsByPolicyId(policyId));
        long countAfterFirst = logRepository.count();

        scheduler.logRecentlyExpiredPolicies();
        long countAfterSecond = logRepository.count();

        assertEquals(countAfterFirst, countAfterSecond);
    }
}
