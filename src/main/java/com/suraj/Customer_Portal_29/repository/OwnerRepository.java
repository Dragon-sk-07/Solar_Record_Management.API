package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.*;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<Owner> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Owner> findByRole(UserRole role);

    @Query("SELECT o FROM Owner o WHERE LOWER(o.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(o.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Owner> searchUsers(@Param("keyword") String keyword);
}