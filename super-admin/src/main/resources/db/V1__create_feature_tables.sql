CREATE EXTENSION IF NOT EXISTS pgcrypto;


                                    CREATE TABLE IF NOT EXISTS features (

                                        id UUID PRIMARY KEY
                                            DEFAULT gen_random_uuid(),

                                        feature_name VARCHAR(150) NOT NULL,

                                        module VARCHAR(100) NOT NULL,

                                        license_plan VARCHAR(100) NOT NULL,

                                        status VARCHAR(20) NOT NULL
                                            DEFAULT 'DISABLED',

                                        configuration JSONB,

                                        created_at TIMESTAMP NOT NULL
                                            DEFAULT CURRENT_TIMESTAMP,

                                        updated_at TIMESTAMP NOT NULL
                                            DEFAULT CURRENT_TIMESTAMP,

                                        created_by UUID,

                                        updated_by UUID,

                                        CONSTRAINT uk_features_feature_name
                                            UNIQUE (feature_name),

                                        CONSTRAINT chk_features_status
                                            CHECK (
                                                status IN (
                                                           'ENABLED',
                                                           'DISABLED'
                                                    )
                                                )
                                    );


                                            CREATE TABLE IF NOT EXISTS feature_assignments (

                                                   id UUID PRIMARY KEY
                                                       DEFAULT gen_random_uuid(),

                                                   feature_id UUID NOT NULL,

                                                   tenant_id UUID NOT NULL,

                                                   organization_id UUID,

                                                   license_plan VARCHAR(100),

                                                   status VARCHAR(20) NOT NULL
                                                       DEFAULT 'ENABLED',

                                                   configuration JSONB,

                                                   assigned_at TIMESTAMP NOT NULL
                                                       DEFAULT CURRENT_TIMESTAMP,

                                                   unassigned_at TIMESTAMP,

                                                   created_at TIMESTAMP NOT NULL
                                                       DEFAULT CURRENT_TIMESTAMP,

                                                   updated_at TIMESTAMP NOT NULL
                                                       DEFAULT CURRENT_TIMESTAMP,

                                                   created_by UUID,

                                                   updated_by UUID,

                                                   CONSTRAINT fk_feature_assignment_feature
                                                       FOREIGN KEY (feature_id)
                                                           REFERENCES features(id)
                                                           ON DELETE CASCADE,

                                                   CONSTRAINT chk_feature_assignment_status
                                                       CHECK (
                                                           status IN (
                                                                      'ENABLED',
                                                                      'DISABLED'
                                                               )
                                                           ),

                                                   CONSTRAINT uk_feature_tenant
                                                       UNIQUE (
                                                               feature_id,
                                                               tenant_id
                                                           )
                                            );

