package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.*;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<Owner> findByEmail(String email);
}
