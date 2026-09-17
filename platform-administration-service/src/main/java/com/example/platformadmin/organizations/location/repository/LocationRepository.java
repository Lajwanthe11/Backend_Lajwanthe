package com.example.platformadmin.organizations.location.repository;

import com.example.platformadmin.organizations.location.entity.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    List<LocationEntity> findByCompanyId(Long companyId);

    List<LocationEntity> findByBranchId(Long branchId);

    boolean existsByCode(String code);

    @Query("""
            SELECT l FROM LocationEntity l
            WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(l.code) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(l.city) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(l.state) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(l.country) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<LocationEntity> search(
            @Param("query") String query,
            Pageable pageable
    );
}