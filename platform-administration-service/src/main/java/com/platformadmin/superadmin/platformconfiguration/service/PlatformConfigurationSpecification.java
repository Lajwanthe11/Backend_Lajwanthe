package com.example.platformadmin.superadmin.platformconfiguration.service;

import com.example.platformadmin.superadmin.platformconfiguration.entity.PlatformConfiguration;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Data JPA Specification builder for dynamic multi-criteria filtering of platform configurations.
 *
 * <p><b>Query Capabilities:</b></p>
 * <ul>
 *   <li>Soft-delete isolation: Automatically excludes soft-deleted records ({@code deleted = false}, BR-0011).</li>
 *   <li>Case-insensitive partial matching on {@code configurationName}.</li>
 *   <li>Normalized exact matching on {@code configurationCategory}, {@code environment}, and {@code status}.</li>
 * </ul>
 */
public final class PlatformConfigurationSpecification {

    private PlatformConfigurationSpecification() {
        // Utility class constructor prevention
    }

    /**
     * Builds a combined dynamic JPA {@link Specification} based on optional filter criteria.
     *
     * @param name        optional configuration name substring filter
     * @param category    optional exact configuration category filter
     * @param environment optional operational environment filter
     * @param status      optional lifecycle status filter
     * @return dynamic JPA {@link Specification} combining all provided filters with logical AND
     */
    public static Specification<PlatformConfiguration> filterBy(
            String name,
            String category,
            String environment,
            String status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records (BR-11)
            predicates.add(cb.isFalse(root.get("deleted")));

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("configurationName")), "%" + name.trim().toLowerCase() + "%"));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("configurationCategory")), category.trim().toUpperCase()));
            }

            if (environment != null && !environment.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("environment")), environment.trim().toUpperCase()));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}