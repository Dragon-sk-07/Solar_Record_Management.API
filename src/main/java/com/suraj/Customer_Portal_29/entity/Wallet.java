package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public enum TransactionType { CREDIT, DEBIT }

    @Entity(name = "WalletTransaction")
    @Table(name = "wallet_transaction")
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

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Wallet getWallet() { return wallet; }
        public void setWallet(Wallet wallet) { this.wallet = wallet; }
        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getBalanceBefore() { return balanceBefore; }
        public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public String getPerformedByEmail() { return performedByEmail; }
        public void setPerformedByEmail(String performedByEmail) { this.performedByEmail = performedByEmail; }
        public PdfDocType getDocType() { return docType; }
        public void setDocType(PdfDocType docType) { this.docType = docType; }
        public String getSolarRecordId() { return solarRecordId; }
        public void setSolarRecordId(String solarRecordId) { this.solarRecordId = solarRecordId; }
        public String getPreviewToken() { return previewToken; }
        public void setPreviewToken(String previewToken) { this.previewToken = previewToken; }
        public boolean isDeleted() { return deleted; }
        public void setDeleted(boolean deleted) { this.deleted = deleted; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }
}