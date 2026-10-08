# ============================================================
# Stage 1: Build
# Builds the full Maven multi-module project so base-service
# is available in the local Maven cache before packaging
# platform-administration-service's fat JAR.
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Install Maven
RUN apk add --no-cache maven

# Copy parent POM first (layer-cache friendly)
COPY pom.xml .

# Copy all module POMs (so dependency:go-offline resolves inter-module deps)
COPY base-service/pom.xml                       base-service/pom.xml
COPY auth-service/pom.xml                       auth-service/pom.xml
COPY platform-administration-service/pom.xml    platform-administration-service/pom.xml

# Resolve all external dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -B --no-transfer-progress \
      -pl base-service,platform-administration-service -am

# Copy sources
COPY base-service/src                        base-service/src
COPY platform-administration-service/src     platform-administration-service/src

# Install base-service to local repo, then package the fat JAR
RUN mvn install -B --no-transfer-progress -pl base-service -DskipTests && \
    mvn package -B --no-transfer-progress -pl platform-administration-service -DskipTests

# ============================================================
# Stage 2: Runtime
# Slim JRE image — only the fat JAR is copied over.
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user for security best-practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder \
  /workspace/platform-administration-service/target/platform-administration-service-*.jar \
  app.jar

RUN chown appuser:appgroup app.jar
USER appuser

# ── Exposed port ──────────────────────────────────────────────
EXPOSE 8082

# ── Environment variables (mirrors application.yml ${VAR:default} placeholders) ──
# Database
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/microservice_db
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=root@123
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update
ENV SPRING_JPA_DEFAULT_SCHEMA=public

# HikariCP connection pool (optional overrides)
ENV DB_POOL_MIN_IDLE=5
ENV DB_POOL_MAX_SIZE=20
ENV DB_POOL_IDLE_TIMEOUT=300000
ENV DB_POOL_MAX_LIFETIME=1200000
ENV DB_POOL_CONN_TIMEOUT=20000

# JWT (MUST match JWT_SECRET in auth-service)
ENV JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
ENV JWT_EXPIRATION_MS=86400000

# Tenant
ENV DEFAULT_TENANT_ID=default
ENV TENANT_HEADER_NAME=X-Tenant-ID

# Spring active profile
ENV SPRING_PROFILES_ACTIVE=default

# ── Entrypoint ────────────────────────────────────────────────
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
