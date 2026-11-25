package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private Owner owner;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    @PrePersist
    public void touch() {
        this.updatedAt = Instant.now();
    }

    public enum TransactionType { CREDIT, DEBIT }

    @Entity(name = "WalletTransaction")
    @Table(name = "wallet_transaction")
    @Getter
    @Setter
    public static class Transaction {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "wallet_id", nullable = false)
        private Wallet wallet;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TransactionType type;

        @Column(nullable = false, precision = 19, scale = 2)
        private BigDecimal amount;

        @Column(nullable = false, precision = 19, scale = 2)
        private BigDecimal balanceBefore;

        @Column(nullable = false, precision = 19, scale = 2)
        private BigDecimal balanceAfter;

        private String note;

        private String performedByEmail;

        @Enumerated(EnumType.STRING)
        private PdfDocType docType;

        private String solarRecordId;

        private String previewToken;

        @Column(nullable = false)
        private boolean deleted = false;

        @Column(nullable = false)
        private Instant createdAt = Instant.now();

        @Column(nullable = false)
        private boolean dummy = false;
    }
}