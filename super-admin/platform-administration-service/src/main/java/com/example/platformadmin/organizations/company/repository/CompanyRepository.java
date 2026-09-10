package com.example.platformadmin.organizations.company.repository;

import com.example.platformadmin.organizations.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("""
            select c from Company c
            where c.deleted = false
              and (lower(c.companyCode) like :pattern escape '\\'
                or lower(c.companyName) like :pattern escape '\\'
                or lower(c.email) like :pattern escape '\\')
            order by c.companyName asc, c.id asc
            """)
    List<Company> searchCompanies(@Param("pattern") String pattern);

    boolean existsByCompanyCodeIgnoreCase(String companyCode);
    boolean existsByCompanyCodeIgnoreCaseAndIdNot(String companyCode, Long id);
}
