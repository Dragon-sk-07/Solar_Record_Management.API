package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    List<UrlEntity> findByUserIdOrderByIsDefaultDescIdDesc(String userId);
    boolean existsByUserId(String userId);
}