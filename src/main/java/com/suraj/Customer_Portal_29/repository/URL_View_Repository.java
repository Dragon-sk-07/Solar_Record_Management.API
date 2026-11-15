package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.URL_View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface URL_View_Repository extends JpaRepository<URL_View, Long> {

    @Query("SELECT u FROM URL_View u " +
            "WHERE u.ownerId IS NULL OR u.ownerId = :ownerId " +
            "ORDER BY u.sortOrder ASC, u.id ASC")
    List<URL_View> findVisibleTo(@Param("ownerId") Long ownerId);

    boolean existsByOwnerIdIsNull();

    List<URL_View> findByOwnerId(Long ownerId);
}