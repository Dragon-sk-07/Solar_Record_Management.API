package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "url_view")
public class URL_View {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * NULL  → global/default URL visible to all vendors
     * value → private URL only visible to that vendor
     */
    @Column(name = "owner_id")
    private Long ownerId;
}