package com.suraj.Customer_Portal_29.dto.response;

import com.suraj.Customer_Portal_29.entity.PdfDocType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class WalletResponseDto {
    private Long walletId;
    private Long ownerId;
    private String ownerEmail;
    private BigDecimal balance;
    private Instant updatedAt;

    private List<PricingItem> pricing;

    @Data
    @Builder
    public static class PricingItem {
        private String docType;
        private String urlSegment;
        private int costInRupees;
        private boolean bundle;
    }

    @Data
    @Builder
    public static class TransactionItem {
        private Long id;
        private String type;
        private BigDecimal amount;
        private BigDecimal balanceBefore;
        private BigDecimal balanceAfter;
        private String docType;
        private String solarRecordId;
        private String note;
        private String performedByEmail;
        private Instant createdAt;
        private String ownerEmail;
    }

    @Data
    @Builder
    public static class PricingListResponse {
        private List<PricingItem> items;
    }

    public static PricingItem toPricingItem(PdfDocType t) {
        return PricingItem.builder()
                .docType(t.name())
                .urlSegment(t.toUrlSegment())
                .costInRupees(t.getCostInRupees())
                .bundle(t == PdfDocType.ALL_IN_ONE || t == PdfDocType.ALL_NINE)
                .build();
    }

    public static TransactionItem toTransactionItem(com.suraj.Customer_Portal_29.entity.Wallet.Transaction tx) {
        return TransactionItem.builder()
                .id(tx.getId())
                .type(tx.getType() != null ? tx.getType().name() : null)
                .amount(tx.getAmount())
                .balanceBefore(tx.getBalanceBefore())
                .balanceAfter(tx.getBalanceAfter())
                .docType(tx.getDocType() != null ? tx.getDocType().name() : null)
                .solarRecordId(tx.getSolarRecordId())
                .note(tx.getNote())
                .performedByEmail(tx.getPerformedByEmail())
                .ownerEmail(tx.getWallet() != null && tx.getWallet().getOwner() != null
                        ? tx.getWallet().getOwner().getEmail() : null)
                .createdAt(tx.getCreatedAt())
                .build();
    }
}