package com.example.carins.repo;

import com.example.carins.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    @Query("select c from Claim c where c.car.id = :carId order by c.claimDate asc")
    List<Claim> findClaimsForCar(@Param("carId") Long carId);
}
