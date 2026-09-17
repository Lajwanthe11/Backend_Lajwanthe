package com.example.microservice.common.abstracts;

import com.example.microservice.common.exception.ResourceNotFoundException;
import com.example.microservice.common.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Generic Abstract Service providing reusable CRUD logic for any domain entity
 * with built-in Multi-Tenancy support.
 * Feature services can extend this class and override mapping/hook methods.
 *
 * @param <T> Entity type
 * @param <ID> Primary key ID type
 * @param <REQ_DTO> Request DTO type
 * @param <RES_DTO> Response DTO type
 */
public abstract class AbstractService<T, ID, REQ_DTO, RES_DTO> implements BaseService<T, ID, REQ_DTO, RES_DTO> {

    protected final JpaRepository<T, ID> repository;
    protected final String resourceName;

    protected AbstractService(JpaRepository<T, ID> repository, String resourceName) {
        this.repository = repository;
        this.resourceName = resourceName;
    }

    /**
     * Map request DTO to database Entity.
     */
    protected abstract T toEntity(REQ_DTO dto);

    /**
     * Map database Entity to response DTO.
     */
    protected abstract RES_DTO toDto(T entity);

    /**
     * Update target entity fields from incoming request DTO.
     */
    protected abstract void updateEntityFromDto(T entity, REQ_DTO dto);

    // Lifecycle hooks for customization
    protected void beforeCreate(T entity, REQ_DTO dto) {}
    protected void afterCreate(T entity, RES_DTO dto) {}
    protected void beforeUpdate(T entity, REQ_DTO dto) {}
    protected void afterUpdate(T entity, RES_DTO dto) {}
    protected void beforeDelete(ID id) {}
    protected void afterDelete(ID id) {}

    protected void validateId(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Resource ID cannot be null");
        }
    }

    /**
     * Get the current active tenant identifier from TenantContext.
     */
    protected String getCurrentTenantId() {
        return TenantContext.getTenantId();
    }

    @Override
    @Transactional
    public RES_DTO create(REQ_DTO requestDto) {
        T entity = toEntity(requestDto);
        if (entity instanceof BaseEntity baseEntity && (baseEntity.getTenantId() == null || baseEntity.getTenantId().isBlank())) {
            baseEntity.setTenantId(getCurrentTenantId());
        }
        beforeCreate(entity, requestDto);
        T savedEntity = repository.save(entity);
        RES_DTO responseDto = toDto(savedEntity);
        afterCreate(savedEntity, responseDto);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public RES_DTO getById(ID id) {
        validateId(id);
        T entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName, "id", id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RES_DTO> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RES_DTO> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public RES_DTO update(ID id, REQ_DTO requestDto) {
        validateId(id);
        T entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName, "id", id));

        updateEntityFromDto(entity, requestDto);
        beforeUpdate(entity, requestDto);
        T updatedEntity = repository.save(entity);
        RES_DTO responseDto = toDto(updatedEntity);
        afterUpdate(updatedEntity, responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        validateId(id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(resourceName, "id", id);
        }
        beforeDelete(id);
        repository.deleteById(id);
        afterDelete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        validateId(id);
        return repository.existsById(id);
    }
}
