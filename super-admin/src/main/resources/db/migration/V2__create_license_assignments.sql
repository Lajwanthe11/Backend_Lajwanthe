CREATE TABLE IF NOT EXISTS license_assignments (
                                                   id UUID DEFAULT gen_random_uuid() NOT NULL,

    license_id UUID NOT NULL,

    tenant_id UUID NOT NULL,

    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    assigned_by UUID NOT NULL,

    revoked_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    created_by UUID NOT NULL,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    updated_by UUID NOT NULL,

    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,

    deleted_at TIMESTAMP,

    deleted_by UUID,

    CONSTRAINT license_assignments_pkey
    PRIMARY KEY (id),

    CONSTRAINT fk_license_assignment_license
    FOREIGN KEY (license_id)
    REFERENCES licenses(id)
    );

CREATE INDEX IF NOT EXISTS idx_license_assignments_license
    ON license_assignments(license_id);

CREATE INDEX IF NOT EXISTS idx_license_assignments_tenant
    ON license_assignments(tenant_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_license_assignment
    ON license_assignments(license_id)
    WHERE revoked_at IS NULL
    AND is_deleted = FALSE;