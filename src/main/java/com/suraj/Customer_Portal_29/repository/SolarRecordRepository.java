package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.SolarRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolarRecordRepository extends JpaRepository<SolarRecord, String> {

}
