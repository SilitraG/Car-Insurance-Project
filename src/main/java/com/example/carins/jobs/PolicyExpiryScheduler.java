package com.example.carins.jobs;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.PolicyExpiryLog;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.PolicyExpiryLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class PolicyExpiryScheduler {
    private static final Logger log = LoggerFactory.getLogger(PolicyExpiryScheduler.class);

    private final InsurancePolicyRepository policyRepository;
    private final PolicyExpiryLogRepository logRepository;

    public PolicyExpiryScheduler(InsurancePolicyRepository policyRepo, PolicyExpiryLogRepository logRepo) {
        this.policyRepository = policyRepo;
        this.logRepository = logRepo;
    }

    @Scheduled(cron = "1 0 0 * * *")
    @Transactional
    public void logRecentlyExpiredPolicies() {
        LocalDate targetEndDate = LocalDate.now().minusDays(1);

        List<InsurancePolicy> targetInsurances = policyRepository.findByEndDate(targetEndDate);

        for (InsurancePolicy p : targetInsurances) {
            if (!logRepository.existsByPolicyId(p.getId())) {
                Long carId = (p.getCar() != null ? p.getCar().getId() : null);
                log.info("Policy {} for car {} expired on {}", p.getId(), carId, p.getEndDate());

                logRepository.save(new PolicyExpiryLog(p));
            }
        }
    }
}
