
CREATE INDEX IF NOT EXISTS idx_features_module
    ON features(module);


CREATE INDEX IF NOT EXISTS idx_features_license_plan
    ON features(license_plan);


CREATE INDEX IF NOT EXISTS idx_features_status
    ON features(status);


CREATE INDEX IF NOT EXISTS idx_feature_assignments_feature_id
    ON feature_assignments(feature_id);


CREATE INDEX IF NOT EXISTS idx_feature_assignments_tenant_id
    ON feature_assignments(tenant_id);


CREATE INDEX IF NOT EXISTS idx_feature_assignments_organization_id
    ON feature_assignments(organization_id);


CREATE INDEX IF NOT EXISTS idx_feature_assignments_license_plan
    ON feature_assignments(license_plan);


CREATE INDEX IF NOT EXISTS idx_feature_assignments_status
    ON feature_assignments(status);
