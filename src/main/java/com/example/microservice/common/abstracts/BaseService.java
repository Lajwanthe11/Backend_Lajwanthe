package com.example.microservice.common.abstracts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Generic Base Service interface defining standard CRUD operations.
 *
 * @param <T> Entity type
 * @param <ID> Primary key ID type
 * @param <REQ_DTO> Request DTO type
 * @param <RES_DTO> Response DTO type
 */
public interface BaseService<T, ID, REQ_DTO, RES_DTO> {

    RES_DTO create(REQ_DTO requestDto);

    RES_DTO getById(ID id);

    List<RES_DTO> getAll();

    Page<RES_DTO> getAll(Pageable pageable);

    RES_DTO update(ID id, REQ_DTO requestDto);

    void deleteById(ID id);

    boolean existsById(ID id);
}
