package com.example.platformadmin.organizations.user.repository;

import com.example.platformadmin.organizations.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}