package com.example.platformadmin.superadmin.feature_management_service.specification;

import com.example.platformadmin.superadmin.feature_management_service.entity.Feature;
import org.springframework.data.jpa.domain.Specification;

public class FeatureSpecification {

    private FeatureSpecification() {
        // Utility class
    }

    /**
     * Search feature by feature name.
     *
     * Example:
     * ?search=User
     *
     * Matches:
     * User Management
     * User Registration
     * User Profile
     */
    public static Specification<Feature> search(String search) {

        return (root, query, criteriaBuilder) -> {

            if (search == null || search.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String value = "%" + search.trim().toLowerCase() + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("featureName")),
                    value
            );
        };
    }

    /**
     * Filter features by module.
     *
     * Example:
     * ?module=HRMS
     */
    public static Specification<Feature> hasModule(String module) {

        return (root, query, criteriaBuilder) -> {

            if (module == null || module.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("module")),
                    module.trim().toLowerCase()
            );
        };
    }

    /**
     * Filter features by license plan.
     *
     * Example:
     * ?licensePlan=ENTERPRISE
     */
    public static Specification<Feature> hasLicensePlan(String licensePlan) {

        return (root, query, criteriaBuilder) -> {

            if (licensePlan == null || licensePlan.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("licensePlan")),
                    licensePlan.trim().toLowerCase()
            );
        };
    }

    /**
     * Filter features by status.
     *
     * Supported values in the current Feature entity:
     * ENABLED
     * DISABLED
     *
     * Example:
     * ?status=ENABLED
     */
    public static Specification<Feature> hasStatus(String status) {

        return (root, query, criteriaBuilder) -> {

            if (status == null || status.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("status")),
                    status.trim().toLowerCase()
            );
        };
    }
}