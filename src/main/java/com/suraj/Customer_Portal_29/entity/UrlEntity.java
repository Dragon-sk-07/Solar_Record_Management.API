package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_urls", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}