# Base Java Spring Boot Microservice

Production-ready Base Java 21 & Spring Boot 3.4.x Microservice template with **Abstract Generic CRUD**, **Spring Security 6**, **OAuth2**, and **JWT Authentication**.

---

## 🏛 Architecture & Project Layout

This project follows the **Package-by-Feature + Shared Common Abstracts** architecture:

```
base-microservice/
├── pom.xml                                         # Java 21, Spring Boot 3.4.x, Spring Data JPA, Security 6, OAuth2, JJWT
└── src/
    └── main/
        ├── java/com/example/microservice/
        │   ├── BaseMicroserviceApplication.java     # Application Entry Point
        │   │
        │   ├── common/                             # Reusable cross-feature infrastructure
        │   │   ├── abstracts/
        │   │   │   ├── BaseEntity.java             # @MappedSuperclass with auto-auditing (ID, timestamps, created/modified by, version)
        │   │   │   ├── BaseService.java            # Generic CRUD interface contract
        │   │   │   ├── AbstractService.java        # Generic CRUD business logic implementation with lifecycle hooks
        │   │   │   └── AbstractController.java     # Generic CRUD REST controller endpoints & helpers
        │   │   │
        │   │   ├── tenant/                         # Multi-Tenancy infrastructure
        │   │   │   ├── TenantContext.java          # InheritableThreadLocal tenant holder
        │   │   │   └── TenantFilter.java           # X-Tenant-ID HTTP header resolution filter
        │   │   │
        │   │   ├── exception/
        │   │   │   ├── AppException.java           # Base application runtime exception
        │   │   │   ├── ResourceNotFoundException.java # 404 handler
        │   │   │   ├── BadRequestException.java    # 400 handler
        │   │   │   ├── ErrorResponse.java          # Uniform error JSON envelope
        │   │   │   └── GlobalExceptionHandler.java # Centralized @RestControllerAdvice
        │   │   │
        │   │   └── response/
        │   │       ├── ApiResponse.java            # Standard JSON response wrapper
        │   │       └── PageResponse.java           # Standard Paginated JSON envelope
        │   │
        │   ├── config/                             # Application configurations
        │   │   ├── AppConfig.java                  # PasswordEncoder & general beans
        │   │   ├── CorsConfig.java                 # Cross-Origin Resource Sharing configuration
        │   │   ├── JpaAuditConfig.java             # JPA Auditing provider connected to SecurityContext
        │   │   └── TenantIdentifierResolver.java   # Hibernate tenant resolver integration
        │   │
        │   └── auth/                               # Authentication feature package
        │       ├── controller/
        │       │   └── AuthController.java         # /auth/login, /auth/register, /auth/refresh, /auth/oauth2/success
        │       ├── service/
        │       │   └── AuthService.java            # Auth operations, password hashing & token issuance
        │       ├── dto/
        │       │   ├── LoginRequest.java
        │       │   ├── RegisterRequest.java
        │       │   ├── AuthResponse.java
        │       │   └── TokenRefreshRequest.java
        │       └── security/
        │           ├── SecurityConfig.java         # Spring Security 6 stateless filter chain & route rules
        │           ├── jwt/
        │           │   ├── JwtTokenProvider.java       # Token generation, claims parser, signature verification
        │           │   ├── JwtAuthenticationFilter.java# Bearer token & tenant context interceptor filter
        │           │   └── JwtAuthenticationEntryPoint.java # 401 Unauthorized JSON handler
        │           ├── oauth2/
        │           │   ├── CustomOAuth2UserService.java # OAuth2 provider user mapper
        │           │   ├── OAuth2AuthenticationSuccessHandler.java # Issues JWT with tenant on social login success
        │           │   └── OAuth2AuthenticationFailureHandler.java
        │           └── user/
        │               ├── UserPrincipal.java          # Unified UserDetails & OAuth2User
        │               └── CustomUserDetailsService.java # Tenant-aware user loader
        └── resources/
            └── application.yml                     # App configuration (H2, JWT, OAuth2, Multi-tenancy, OpenAPI)
```

---

## 🚀 How to Add a New Feature / Domain Model in 3 Minutes

When building a new microservice or adding a feature (e.g. `product`, `customer`, `order`), simply follow the **Package-by-Feature** approach:

### 1. Create your Entity extending `BaseEntity`
```java
package com.example.microservice.product;

import com.example.microservice.common.abstracts.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class ProductEntity extends BaseEntity {
    private String name;
    private Double price;
    private String sku;
}
```

### 2. Create DTOs
```java
public record ProductRequestDto(String name, Double price, String sku) {}
public record ProductResponseDto(Long id, String name, Double price, String sku) {}
```

### 3. Create Repository
```java
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {}
```

### 4. Create Service extending `AbstractService`
```java
@Service
public class ProductService extends AbstractService<ProductEntity, Long, ProductRequestDto, ProductResponseDto> {

    public ProductService(ProductRepository repository) {
        super(repository, "Product");
    }

    @Override
    protected ProductEntity toEntity(ProductRequestDto dto) {
        ProductEntity entity = new ProductEntity();
        entity.setName(dto.name());
        entity.setPrice(dto.price());
        entity.setSku(dto.sku());
        return entity;
    }

    @Override
    protected ProductResponseDto toDto(ProductEntity entity) {
        return new ProductResponseDto(entity.getId(), entity.getName(), entity.getPrice(), entity.getSku());
    }

    @Override
    protected void updateEntityFromDto(ProductEntity entity, ProductRequestDto dto) {
        entity.setName(dto.name());
        entity.setPrice(dto.price());
        entity.setSku(dto.sku());
    }
}
```

### 5. Create Controller extending `AbstractController`
```java
@RestController
@RequestMapping("/products")
public class ProductController extends AbstractController<ProductEntity, Long, ProductRequestDto, ProductResponseDto> {

    public ProductController(ProductService productService) {
        super(productService);
    }
}
```

✨ **Done!** You now immediately have full REST CRUD endpoints:
- `POST /api/products` (Create)
- `GET /api/products/{id}` (Get by ID)
- `GET /api/products?page=0&size=20&sort=id,desc` (Paginated list)
- `GET /api/products/all` (Unpaged list)
- `PUT /api/products/{id}` (Update)
- `DELETE /api/products/{id}` (Delete)

---

## 🔐 Authentication & Security

### Default Seeded Users (for testing)
- **Admin**: `username`: `admin`, `password`: `admin123`
- **User**: `username`: `user`, `password`: `user123`

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-abc" \
  -d '{
    "username": "johndoe",
    "email": "johndoe@example.com",
    "password": "secretPassword123"
  }'
```

### 2. Login & Obtain JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-abc" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "username": "admin",
    "tenantId": "tenant-abc",
    "roles": ["ROLE_ADMIN", "ROLE_USER"]
  }
}
```

### 3. Access Secured Endpoints
Include the token in the `Authorization` header:
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer <your-access-token>" \
  -H "X-Tenant-ID: tenant-abc"
```

### 4. Refresh Access Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<your-refresh-token>"
  }'
```

---

## 🏷️ JWT Token Structure & Details Extraction

Every JWT token issued by `JwtTokenProvider` carries the user identity, multi-tenancy identifier, and permissions.

### JWT Payload Claims
| Claim Key | Description | Source / Default |
| :--- | :--- | :--- |
| `sub` | **Username** (subject) | `UserPrincipal.getUsername()` |
| `tenantId` | **Tenant Identifier** | `TenantContext.getTenantId()` (default: `"default"`) |
| `roles` | **Roles & Authorities** (comma-separated, e.g. `ROLE_ADMIN,ROLE_USER`) | `Authentication.getAuthorities()` |
| `iat` | **Issued At timestamp** | Generated at token creation |
| `exp` | **Expiration timestamp** | Configured via `app.jwt.expiration-ms` |

### Extracting Details in Code

#### 1. In Any Service or Utility
```java
// Get current tenant anywhere in the request thread
String currentTenant = TenantContext.getTenantId();

// Extract directly from a raw token using JwtTokenProvider
String username = jwtTokenProvider.getUsernameFromJWT(token);
String tenantId = jwtTokenProvider.getTenantIdFromJWT(token);
String roles    = jwtTokenProvider.getRolesFromJWT(token);
```

#### 2. In a Controller using `@AuthenticationPrincipal` or `SecurityContext`
```java
@RestController
@RequestMapping("/profile")
public class UserProfileController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        Map<String, Object> details = Map.of(
            "username", currentUser.getUsername(),
            "email", currentUser.getEmail(),
            "tenantId", currentUser.getTenantId(),
            "roles", currentUser.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
        );
        return ResponseEntity.ok(ApiResponse.ok("User profile fetched", details));
    }
}
```

---

## 🔄 Propagating Authentication to Other Microservices

When this service needs to call downstream microservices, you can pass authentication and tenant context using **Spring Cloud OpenFeign**, Spring 6 **`RestClient`**, or **`WebClient`**.

### Strategy 1: Forwarding the JWT Bearer Token (Recommended)

#### A. Using Spring Cloud OpenFeign
```java
package com.example.microservice.config;

import com.example.microservice.common.tenant.TenantContext;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor bearerTokenAndTenantInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 1. Forward Authorization Bearer Header
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && !authHeader.isBlank()) {
                    requestTemplate.header("Authorization", authHeader);
                }
            }

            // 2. Forward Tenant Header
            requestTemplate.header("X-Tenant-ID", TenantContext.getTenantId());
        };
    }
}
```

#### B. Using Spring Boot 3 / Spring 6 `RestClient`
```java
package com.example.microservice.config;

import com.example.microservice.common.tenant.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient downstreamRestClient(RestClient.Builder builder) {
        return builder
            .requestInterceptor((request, body, execution) -> {
                ServletRequestAttributes attrs = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                if (attrs != null) {
                    String authHeader = attrs.getRequest().getHeader("Authorization");
                    if (authHeader != null && !authHeader.isBlank()) {
                        request.getHeaders().set("Authorization", authHeader);
                    }
                }
                
                request.getHeaders().set("X-Tenant-ID", TenantContext.getTenantId());
                return execution.execute(request, body);
            })
            .build();
    }
}
```

### Strategy 2: Gateway / Context Header Propagation
If downstream microservices reside within a private network and trust internal calls, forward resolved identity attributes as standard headers:

```java
@Bean
public RequestInterceptor internalContextHeadersInterceptor() {
    return requestTemplate -> {
        // Forward Tenant ID
        requestTemplate.header("X-Tenant-ID", TenantContext.getTenantId());

        // Forward Authenticated Username & Roles
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            requestTemplate.header("X-User-Name", principal.getUsername());
            requestTemplate.header("X-User-Email", principal.getEmail());
            requestTemplate.header("X-User-Roles", principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(",")));
        }
    };
}
```

---

## 🌐 OAuth2 Social Login Integration

Configure your OAuth2 Client credentials in `application.yml` or via environment variables:
- `OAUTH2_GOOGLE_CLIENT_ID` / `OAUTH2_GOOGLE_CLIENT_SECRET`
- `OAUTH2_GITHUB_CLIENT_ID` / `OAUTH2_GITHUB_CLIENT_SECRET`

Login URLs:
- Google: `http://localhost:8080/api/oauth2/authorization/google`
- GitHub: `http://localhost:8080/api/oauth2/authorization/github`

Upon successful social authentication, `OAuth2AuthenticationSuccessHandler` automatically generates a JWT access token & refresh token with the active tenant ID and redirects the browser to the configured frontend callback URL:
`http://localhost:3000/oauth2/redirect?token=<accessToken>&refreshToken=<refreshToken>&tenantId=<tenantId>`

---

## 📑 Swagger UI & OpenAPI Docs

Access interactive Swagger documentation at:
`http://localhost:8080/api/swagger-ui/index.html`

