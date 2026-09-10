CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE license_status AS ENUM (
    'PENDING',
    'ACTIVE',
    'SUSPENDED',
    'EXPIRED'
);

CREATE TABLE licenses (
                          id UUID DEFAULT gen_random_uuid() NOT NULL,

                          license_key VARCHAR(100) NOT NULL,

                          license_plan VARCHAR(100) NOT NULL,

                          license_type VARCHAR(50),

                          activation_date DATE NOT NULL,

                          expiry_date DATE NOT NULL,

                          status license_status DEFAULT 'PENDING' NOT NULL,

                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

                          created_by UUID NOT NULL,

                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

                          updated_by UUID NOT NULL,

                          is_deleted BOOLEAN DEFAULT FALSE NOT NULL,

                          deleted_at TIMESTAMP,

                          deleted_by UUID,

                          CONSTRAINT licenses_pkey
                              PRIMARY KEY (id),

                          CONSTRAINT licenses_license_key_key
                              UNIQUE (license_key),

                          CONSTRAINT chk_license_dates
                              CHECK (expiry_date > activation_date)
);

CREATE INDEX idx_licenses_expiry_date
    ON licenses(expiry_date);

CREATE INDEX idx_licenses_license_plan
    ON licenses(license_plan);

CREATE INDEX idx_licenses_status
    ON licenses(status);