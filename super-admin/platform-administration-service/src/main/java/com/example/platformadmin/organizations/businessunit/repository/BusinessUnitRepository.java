package com.example.platformadmin.organizations.businessunit.repository;

import com.example.platformadmin.organizations.businessunit.entity.BusinessUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, Long> {

    Optional<BusinessUnit> findByIdAndIsDeletedFalse(Long id);

    List<BusinessUnit> findByIsDeletedFalse();

    Page<BusinessUnit> findByIsDeletedFalse(Pageable pageable);

    boolean existsByUnitCodeAndIsDeletedFalse(String unitCode);

    @Query("SELECT b FROM BusinessUnit b WHERE b.isDeleted = false AND (" +
           "LOWER(b.unitCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.unitName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<BusinessUnit> searchBusinessUnits(@Param("query") String query);
}
