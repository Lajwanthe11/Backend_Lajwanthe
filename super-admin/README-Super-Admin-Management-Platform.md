# Super Admin Management Platform

## Overview

The **Super Admin Management Platform** is an enterprise backend solution composed of four functional modules:

1. **Platform Branding** — manages platform-wide identity, appearance, and branding assets.
2. **Feature Management** — manages platform features, their configuration, activation state, and tenant/organization assignment.
3. **License Management** — manages license creation, lifecycle, renewal, and tenant assignment.
4. **Platform Health Overview** — provides read-only health and availability information for the platform and its registered services.

The modules are implemented with Spring Boot and expose REST APIs for Super Admin and platform-level operations.

> **Architecture note:** The supplied module READMEs use both "modules/services" and "microservices" terminology. They also contain different runtime ports and database names. This README preserves those module-level facts and calls out the inconsistencies rather than silently inventing a single runtime configuration.

---

## Platform Capabilities

| Module | Primary Responsibility | Base API |
|---|---|---|
| Platform Branding | Platform identity and branding configuration | `/api/v1/branding` |
| Feature Management | Feature catalogue, lifecycle and assignment | `/api/v1/features` |
| License Management | License lifecycle and tenant assignment | `/api/v1/licenses` |
| Platform Health | Platform/service/database health aggregation | `/api/v1/health` |

---

# 1. High-Level Architecture

```text
                         ┌──────────────────────┐
                         │   API Client / UI    │
                         │   Postman / Frontend │
                         └──────────┬───────────┘
                                    │
                                    ▼
                    ┌──────────────────────────────┐
                    │     Super Admin Platform     │
                    │       Spring Boot APIs       │
                    └──────────────┬───────────────┘
                                   │
             ┌─────────────────────┼─────────────────────┐
             │                     │                     │
             ▼                     ▼                     ▼
   ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
   │ Platform        │   │ Feature         │   │ License         │
   │ Branding        │   │ Management      │   │ Management      │
   └────────┬────────┘   └────────┬────────┘   └────────┬────────┘
            │                     │                     │
            │                     │                     │
            │              ┌──────┴───────┐      ┌─────┴──────┐
            │              │ Tenant       │      │ Tenant     │
            │              │ / License    │      │ / Org      │
            │              │ integrations │      │ assignment │
            │              └──────────────┘      └────────────┘
            │
            ▼
   ┌─────────────────┐
   │ Configuration / │
   │ Storage / Audit │
   └─────────────────┘

                         ┌─────────────────┐
                         │ Platform Health │
                         │   Overview      │
                         └────────┬────────┘
                                  │
                    ┌─────────────┼──────────────┐
                    ▼             ▼              ▼
               Actuator        Eureka       Service APIs
               health          discovery    / DB health
```

### Internal request pattern

For business modules, the expected flow is:

```text
Client
  ↓
Controller
  ↓
DTO / Validation
  ↓
Service
  ↓
Repository / Integration
  ↓
Database or External Service
  ↓
Response DTO
```

Platform Health is read-oriented and uses runtime service-health sources rather than changing business data.

---

# 2. Technology Stack

The supplied module documentation identifies the following common technologies:

| Technology | Usage |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 for the current Branding/Feature documentation; License documentation also contains an older 3.4.5 reference |
| Spring Web MVC | REST APIs |
| Spring Data JPA | Persistence |
| PostgreSQL | Relational database |
| Spring Security | Authentication and authorization |
| Bean Validation | Request validation |
| Flyway | Database migrations where configured |
| Spring Boot Actuator | Monitoring and health |
| SpringDoc OpenAPI | Swagger/OpenAPI documentation |
| Eureka | Service discovery for Platform Health |
| JUnit 5 | Testing |
| Mockito | Mocking in Platform Health |
| MockMvc | Controller testing |
| Lombok | Boilerplate reduction |
| Maven | Build and dependency management |

> **Important:** Spring Boot version is not consistent across the supplied READMEs. Feature/Branding documentation states **4.1.1**, while the License README states **3.4.5**. Verify the actual root/module `pom.xml` files before standardizing the project version.

---

# 3. Module 1 — Platform Branding

## Purpose

Platform Branding manages the platform-wide identity and appearance used by approved platform consumers.

### Capabilities

- Platform name
- Company name
- Tagline
- Company logo
- Login background
- Welcome message
- Primary, secondary and accent colors
- Light/Dark theme
- Favicon
- Email header logo
- Footer text
- Copyright text
- Preview
- Reset
- Publish

## Architecture

```text
Controller
   ↓
Branding DTOs
   ↓
Branding Service
   ↓
Configuration Repository
   ↓
public.configurations

Asset APIs
   ↓
StorageClient
   ↓
Approved Storage Provider

Branding mutations
   ↓
AuditLogClient
   ↓
Audit capability
```

## Persistence

Branding reuses the existing configuration structure:

```text
category = BRANDING
scope    = PLATFORM
```

Configuration keys include:

```text
branding.platform_name
branding.company_name
branding.tagline
branding.logo_url
branding.login_background_url
branding.welcome_message
branding.primary_color
branding.secondary_color
branding.accent_color
branding.theme
branding.favicon_url
branding.email_header_logo_url
branding.footer_text
branding.copyright_text
branding.status
```

Branding lifecycle:

```text
DRAFT → PUBLISHED
```

The database configuration record itself is maintained as active while the branding lifecycle is represented by `branding.status`.

## API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/branding` | Get current branding |
| POST | `/api/v1/branding` | Create/initialize branding |
| PUT | `/api/v1/branding` | Update branding |
| POST | `/api/v1/branding/preview` | Preview changes without persistence |
| POST | `/api/v1/branding/reset` | Reset branding |
| POST | `/api/v1/branding/publish` | Publish draft branding |

## Branding validation

- Platform name: required, max 100 characters
- Company name: required, max 100 characters
- Tagline: max 255 characters
- Welcome message: max 250 characters
- Footer text: max 200 characters
- Copyright text: max 200 characters
- Colors: `#RRGGBB`
- Theme: `LIGHT` or `DARK`

### Assets

**Logo**

```text
PNG / JPG / JPEG / SVG
Maximum: 5 MB
```

**Login background**

```text
PNG / JPG / JPEG
Maximum: 10 MB
```

The asset layer validates file presence, extension, MIME type and size.

## Security and Audit

Branding modification is restricted to authenticated Super Administrators and currently uses the `SUPER_ADMIN` role in the local implementation.

Administrative actions are resolved through the authenticated user context and sent through `AuditLogClient`.

Audit actions include:

```text
BRANDING_CREATED
BRANDING_UPDATED
BRANDING_RESET
BRANDING_PUBLISHED
```

### Local development

The supplied Branding README documents:

```text
Application: http://localhost:8080
Profile: dev
```

Local development authentication is documented as a development-only Super Admin account. Do not use development credentials in production.

---

# 4. Module 2 — Feature Management

## Purpose

Feature Management controls the platform feature catalogue, feature configuration, lifecycle state and assignment of features to tenants/organizations.

### Capabilities

- Create features
- Retrieve features
- Update features
- Activate/deactivate features
- Configure feature settings
- Assign features to tenants
- Retrieve tenant feature status
- License-plan-based feature handling
- Organization association

## Module structure

```text
feature_management_service/
├── config/
├── controller/
│   ├── FeatureController
│   └── FeatureAssignmentController
├── dto/
│   ├── request/
│   └── response/
├── entity/
│   ├── Feature
│   └── FeatureAssignment
├── exception/
├── integration/
│   ├── LicenseServiceClient
│   └── TenantServiceClient
├── repository/
│   ├── FeatureRepository
│   └── FeatureAssignmentRepository
└── services/
    ├── FeatureService
    └── FeatureAssignmentService
```

## Feature model

A feature includes data such as:

```text
id
featureName
module
licensePlan
status
configuration
createdAt
updatedAt
createdBy
updatedBy
```

Feature states:

```text
ENABLED
DISABLED
```

Feature assignment supports tenant and organization context and includes assignment/configuration metadata.

## API

### Feature APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/features` | Create feature |
| GET | `/api/v1/features` | List features |
| PUT | `/api/v1/features/{id}` | Update feature |
| PATCH | `/api/v1/features/{id}/activate` | Activate feature |
| PATCH | `/api/v1/features/{id}/deactivate` | Deactivate feature |

### Assignment APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/features/assign` | Assign feature |
| GET | `/api/v1/features/tenant/{tenantId}` | Get tenant feature status |

## Integrations

The supplied documentation identifies integration contracts for:

```text
TenantServiceClient
LicenseServiceClient
```

Examples include:

```text
tenantExists()
organizationBelongsToTenant()
isLicenseEligible()
```

The documentation describes these as service integration contracts; complete remote integration is identified as a future item in the supplied Feature README.

## Database

Feature Management documents two owned tables:

```text
features
feature_assignments
```

Flyway migrations documented for the module:

```text
V1__create_feature_tables.sql
V2__create_feature_indexes.sql
```

Important indexes are documented for feature module/license/status and assignment feature/tenant/organization/license/status fields.

---

# 5. Module 3 — License Management

## Purpose

License Management manages software licenses, their lifecycle, renewal, expiry and tenant assignment.

## License model

```text
id
license_key
license_plan
license_type
activation_date
expiry_date
status
created_at
updated_at
created_by
updated_by
is_deleted
deleted_at
deleted_by
```

License assignment includes:

```text
id
license_id
tenant_id
assigned_at
assigned_by
revoked_at
```

## License states

```text
PENDING
ACTIVE
SUSPENDED
EXPIRED
```

## License types

```text
SUBSCRIPTION
PERPETUAL
TRIAL
ENTERPRISE
```

## API

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/licenses` | Create license |
| GET | `/api/v1/licenses` | List/search licenses |
| GET | `/api/v1/licenses/{licenseId}` | Get license by ID |
| GET | `/api/v1/licenses/key/{licenseKey}` | Get license by key |
| PUT | `/api/v1/licenses/{licenseId}` | Update license |
| PATCH | `/api/v1/licenses/{licenseId}/activate` | Activate license |
| PATCH | `/api/v1/licenses/{licenseId}/reactivate` | Reactivate license |
| PATCH | `/api/v1/licenses/{licenseId}/suspend` | Suspend license |
| PATCH | `/api/v1/licenses/{licenseId}/renew` | Renew license |
| GET | `/api/v1/licenses/{licenseId}/status` | Get license status |
| POST | `/api/v1/licenses/{licenseId}/assign` | Assign license to tenant |
| DELETE | `/api/v1/licenses/{licenseId}/assign/{tenantId}` | Revoke assignment |

## Query filters

The documented list API supports:

```text
plan
status
```

Example:

```http
GET /api/v1/licenses?plan=PREMIUM&status=ACTIVE
```

## License lifecycle

```text
PENDING
   │
   ▼
ACTIVE
   │
   ├──► SUSPENDED ──► ACTIVE
   │
   └──► EXPIRED
```

Renewal updates the expiry date of an existing license.

## Security

The supplied License README documents Spring Security with OAuth2 Resource Server/JWT validation and identifies public documentation/health endpoints.

The README also documents an optional `X-Actor-Id` header for some lifecycle operations.

> **Verification required:** The supplied License README contains an older Spring Boot/security configuration than the Feature and Branding documentation. Verify the actual License `pom.xml` and `SecurityConfig` before using this section as a deployment contract.

---

# 6. Module 4 — Platform Health Overview

## Purpose

Platform Health is a **read-only** operational module that aggregates platform, service and database health information.

### Capabilities

- Overall platform health
- All registered services health
- Individual service health
- Database health
- Service availability
- Degraded/unavailable detection
- Response-time information where applicable

## Architecture

```text
Platform Health Controller
        ↓
Health Aggregation Services
        ↓
Integration Clients
   ┌────┼────┐
   ↓    ↓    ↓
Actuator Eureka Service APIs
        ↓
Aggregated health response
```

## Health states

```text
HEALTHY
DEGRADED
```

Availability is represented separately.

## API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/health` | Overall platform health |
| GET | `/api/v1/health/services` | Health of all registered services |
| GET | `/api/v1/health/services/{serviceName}` | Health of one service |
| GET | `/api/v1/health/database` | Database health |

## Runtime sources

The supplied Health README documents:

- Spring Boot Actuator
- Eureka Service Discovery
- Service Health APIs
- Approved operational metrics

Eureka is documented at:

```text
http://localhost:8761/eureka/
```

The Health module communicates with other services through APIs rather than directly querying their business databases.

## Security

The supplied Health README documents JWT authentication with:

```text
PLATFORM_HEALTH_READ
```

authority for:

```text
/api/v1/health/**
```

Example:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# 7. Cross-Module Relationships

The functional relationships documented across the four modules are:

```text
                 ┌────────────────────┐
                 │ Platform Branding  │
                 └────────────────────┘
                           │
                    Published branding
                           │
                           ▼
                 Platform UI / Consumers


┌─────────────────┐      license      ┌─────────────────┐
│ Feature         │ ◄───────────────► │ License         │
│ Management      │                  │ Management      │
└────────┬────────┘                  └────────┬────────┘
         │                                    │
         │ tenant / organization              │ tenant assignment
         ▼                                    ▼
     Tenant Domain / Organization Domain


                 ┌────────────────────┐
                 │ Platform Health    │
                 └─────────┬──────────┘
                           │
             runtime health / availability
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
      Branding         Features         Licenses
```

The Feature module documents License and Tenant integration contracts, while Platform Health uses runtime service-health APIs/discovery.

---

# 8. Security Model

Security varies slightly across the supplied module documentation and therefore requires final project-level standardization.

### Documented authorization concepts

```text
Super Administrator
SUPER_ADMIN
PLATFORM_HEALTH_READ
JWT / Bearer authentication
Spring Security
```

### Principles

- Protected APIs require authentication.
- Administrative mutations require server-side authorization.
- UI visibility is not authorization.
- Actor identity should come from the authenticated security context where that capability is implemented.
- Secrets must not be committed to source control.
- Production authentication should use the approved centralized security capability.

---

# 9. Error Handling

The supplied modules document centralized exception handling for domain and validation failures.

Common response categories include:

```text
200 OK
201 Created
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

The Platform Branding project follows the common enterprise error model with stable error codes, safe messages, correlation identifiers and validation details where applicable.

Typical domain failures include:

```text
Not Found
Already Exists / Duplicate
Invalid State
Validation Failure
```

---

# 10. Database and Persistence

The project uses PostgreSQL across the modules, but the supplied module READMEs document different local database names.

### Branding

```text
cloud_platform
```

Uses:

```text
public.configurations
```

### Feature Management

```text
enterprise_platform
```

Documents owned:

```text
features
feature_assignments
```

### License Management

The supplied environment example references:

```text
cloud_platform
```

with license-owned:

```text
licenses
license_assignments
```

### Platform Health

Uses its configured PostgreSQL connection for database-health checking and does not own another module's business tables.

> **Important:** Standardize the actual database topology from the repository's configuration and migration files before using this README as the deployment source of truth.

---

# 11. Database Migration

Flyway is documented for Feature Management and License Management, while Branding explicitly reuses the existing configuration structure.

General migration rule:

```text
Application
   ↓
Flyway
   ↓
Versioned migrations
   ↓
PostgreSQL
```

Database changes should be reviewed and represented by versioned migrations rather than being applied manually in production.

---

# 12. Configuration

Each module documents configuration through:

```text
src/main/resources/application.properties
```

Environment-specific values should be externalized.

Example:

```properties
DB_URL=<DATABASE_URL>
DB_USERNAME=<DATABASE_USERNAME>
DB_PASSWORD=<DATABASE_PASSWORD>
JWT_ISSUER_URI=<JWT_ISSUER>
TENANT_SERVICE_URL=<TENANT_SERVICE_URL>
LICENSE_SERVICE_URL=<LICENSE_SERVICE_URL>
EUREKA_URL=<EUREKA_URL>
```

Only define variables that are actually referenced by the corresponding module.

Never commit:

```text
database passwords
JWT secrets
API tokens
private keys
production credentials
```

---

# 13. Local Development Ports

The four supplied READMEs do not currently agree on ports.

| Module | Documented local port |
|---|---:|
| Platform Branding | 8080 |
| Feature Management | 8083 |
| License Management | 8083 |
| Platform Health | 8080 |

These values create conflicts when the modules are expected to run together on one host.

### Recommended action

Before integration/deployment, confirm:

```text
Service name
Port
Context path
Database
Gateway route
Discovery registration name
```

and maintain the final values in the actual project configuration rather than in manually copied documentation.

---

# 14. API Documentation / Swagger

The Feature and License documentation references SpringDoc/OpenAPI.

Typical endpoints documented by the modules include:

```text
/v3/api-docs
/swagger-ui.html
/swagger-ui/**
```

The exact exposure/security policy must be verified against each module's actual security configuration.

---

# 15. Monitoring

Platform Health uses runtime monitoring sources including:

```text
/actuator/health
```

The supplied Feature README also documents Actuator.

Health monitoring is intended to be read-only and should not modify business data.

---

# 16. Testing

The supplied module READMEs document testing using:

```bash
./mvnw test
```

or:

```bash
mvn test
```

Documented testing areas include:

### Platform Branding

- Validation
- Service behavior
- Controller/API behavior
- Authorization
- Asset validation/storage
- Branding lifecycle

### Feature Management

- Controller behavior
- Service logic
- Repository operations
- Feature lifecycle
- Assignment behavior

### License Management

- Controller/API routes
- Repository queries
- Service lifecycle logic
- Assignment behavior

### Platform Health

- Overall health
- Service health
- Individual service health
- Database health
- Aggregation
- Unavailable services
- Controller/service behavior
- Exception handling
- Security/authorization

---

# 17. Postman / API Testing

Postman collections are documented for the individual modules.

### Branding

```text
Get Current Branding
Create Branding
Update Branding
Preview Branding
Reset Branding
Publish Branding
Verify Published Branding

Upload/Replace/Delete branding assets
```

### Feature Management

```text
Create Feature
Get All Features
Update Feature
Activate Feature
Deactivate Feature
Assign Feature to Tenant
Get Features by Tenant
```

### License Management

The module README documents REST endpoints and curl examples for:

```text
Create
List
Get by ID
Get by key
Update
Activate
Reactivate
Suspend
Renew
Status
Assign
Revoke assignment
```

### Platform Health

```text
Get Platform Health
Get All Services Health
Get Service Health
Get Database Health
```

Postman authentication must use the same authentication model as the running module. Never commit real JWT tokens or production credentials.

---

# 18. Build and Run

Each module uses Maven/Maven Wrapper based on its supplied documentation.

### Windows

```bash
mvnw.cmd clean install
```

Run:

```bash
mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw clean install
```

Run:

```bash
./mvnw spring-boot:run
```

Or use Maven directly when Maven is installed:

```bash
mvn clean install
mvn spring-boot:run
```

---

# 19. Developer Workflow

Typical development workflow:

```text
Create feature branch
        ↓
Implement module change
        ↓
Run tests
        ↓
Check git status
        ↓
Commit
        ↓
Push branch
        ↓
Create Pull Request
        ↓
Code Review
        ↓
CI validation
        ↓
Merge
```

Example:

```bash
git status
git add .
git commit -m "<professional commit message>"
git push origin <feature-branch>
```

Avoid committing:

```text
target/
.idea/
*.iml
local credentials
database passwords
JWT secrets
environment-specific secrets
```

---

# 20. Architecture Principles

The supplied module documentation consistently emphasizes:

### Separation of concerns

Controllers handle HTTP concerns.

Services handle business rules.

Repositories handle persistence.

DTOs define external contracts.

Integration clients communicate with other services.

### No cross-service database shortcuts

Services should use approved APIs/contracts instead of directly querying another service's business tables.

### API contracts first

HTTP method, URI, authentication, authorization, parameters, DTOs, validation, statuses and error codes should be agreed before dependent services are built.

### Versioned database changes

Database changes should use reviewed, versioned migrations.

### Production quality

Production delivery should include:

```text
Validation
Authorization
Error handling
Testing
Audit
Integration
Documentation
```

---

# 21. Known Inconsistencies Requiring Project-Level Confirmation

The four supplied READMEs are useful module documentation, but they are not yet a single authoritative deployment document.

The following differences must be confirmed:

| Area | Inconsistency |
|---|---|
| Architecture terminology | Modules/services vs independent microservices |
| Spring Boot | 4.1.1 vs 3.4.5 |
| Ports | 8080 vs 8083 |
| Database names | `cloud_platform` vs `enterprise_platform` |
| Security | Local Basic/Super Admin vs JWT/OAuth2 documentation |
| Actor handling | Authenticated context vs `X-Actor-Id` in License documentation |
| Feature integrations | Some are documented as contracts/future integrations |
| Production infrastructure | Gateway, centralized auth, Docker/CI/tracing/logging are not consistently documented as completed |

These should be resolved from the actual source repositories and final architecture decision before treating this README as the definitive production deployment guide.

---

# 22. Production Readiness Checklist

Before production release, verify:

```text
[ ] Final service/module topology approved
[ ] Unique production ports/routes configured
[ ] API Gateway routes configured
[ ] Service discovery configured where required
[ ] Central authentication/RBAC integrated
[ ] Production secrets externalized
[ ] Database configuration standardized
[ ] Flyway/migrations verified
[ ] Storage provider approved and configured
[ ] Audit integration verified
[ ] Cross-service API contracts finalized
[ ] Postman collections updated to final URLs
[ ] OpenAPI documentation verified
[ ] Positive/negative/edge tests passing
[ ] CI pipeline passing
[ ] Logging and monitoring configured
[ ] Distributed tracing configured where required
[ ] Production configuration reviewed
```

---

# 23. Quick Reference

| Module | API Base | Main Purpose |
|---|---|---|
| Platform Branding | `/api/v1/branding` | Platform identity and branding |
| Feature Management | `/api/v1/features` | Feature catalogue and assignment |
| License Management | `/api/v1/licenses` | License lifecycle and assignment |
| Platform Health | `/api/v1/health` | Platform/service health |

### Core domains

```text
Branding
Features
Licenses
Health
```

### Core infrastructure

```text
Java 21
Spring Boot
Spring Web MVC
Spring Data JPA
Spring Security
PostgreSQL
Flyway
Actuator
OpenAPI
Eureka
Maven
JUnit 5
```

---

# 24. Summary

The Super Admin Management Platform provides four complementary capabilities:

```text
Platform Branding
        +
Feature Management
        +
License Management
        +
Platform Health
```

Together they support platform administration, configuration, licensing, feature availability and operational visibility.

This README is a **merged project-level view of the four supplied module READMEs**. It intentionally preserves documented module responsibilities while identifying configuration and architecture conflicts that should be resolved against the actual repositories and approved project architecture before production deployment.
