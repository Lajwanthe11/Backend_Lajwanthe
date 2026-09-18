package com.example.rbac.repository;

import com.example.rbac.entity.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

//  Spring Data JPA repository used to save and retrieve security events.
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

    Page<SecurityEvent> findAllByOrderByTimestampDesc(Pageable pageable);
}
