package com.example.platformadmin.organizations.costcenter.repository;

import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CostCenterRepository extends JpaRepository<CostCenterEntity, Long>
{
    Optional<CostCenterEntity> findByCostCenterCode(String costCenterCode);
    boolean existsByCostCenterCode(String costCenterCode);
}