package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.SolarRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SolarRecordRepository extends JpaRepository<SolarRecord, String> {
    Optional<SolarRecord> findByConsumerNumber(String consumerNumber);
    List<SolarRecord> findByCreatedById(Long userId);
    List<SolarRecord> findByCreatedByUserEmail(String userEmail);
}