package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByEmail(String email);
}
