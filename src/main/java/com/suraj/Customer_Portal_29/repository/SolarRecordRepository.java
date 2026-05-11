package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.SolarRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolarRecordRepository extends JpaRepository<SolarRecord, String> {
    List<SolarRecord> findByCreatedByUserEmail(String userEmail);
    List<SolarRecord> findByCreatedByUserId(Long userId);

}
