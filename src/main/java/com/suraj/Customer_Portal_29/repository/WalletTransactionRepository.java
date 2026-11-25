package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<Wallet.Transaction, Long> {

    Page<Wallet.Transaction> findByWalletIdAndDeletedFalseOrderByCreatedAtDesc(
            Long walletId, Pageable pageable);

    List<Wallet.Transaction> findTop50ByWalletIdAndDeletedFalseOrderByCreatedAtDesc(Long walletId);

    List<Wallet.Transaction> findAllByDeletedFalseOrderByCreatedAtDesc();

    boolean existsByPreviewTokenAndDeletedFalse(String previewToken);

    @Modifying
    @Transactional
    @Query("UPDATE WalletTransaction t SET t.deleted = true WHERE t.wallet.id = :walletId")
    int softDeleteByWalletId(@Param("walletId") Long walletId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM WalletTransaction t WHERE t.type = :type")
    BigDecimal sumByType(@Param("type") Wallet.TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM WalletTransaction t " +
            "WHERE t.wallet.owner.id = :ownerId AND t.type = :type " +
            "AND t.deleted = false AND t.dummy = false")
    BigDecimal sumAmountByOwnerIdAndType(@Param("ownerId") Long ownerId,
                                         @Param("type") Wallet.TransactionType type);

    @Query("SELECT t.wallet.owner.id, COALESCE(SUM(t.amount), 0) " +
            "FROM WalletTransaction t " +
            "WHERE t.type = :type AND t.wallet.owner.id IN :ids " +
            "AND t.deleted = false AND t.dummy = false " +
            "GROUP BY t.wallet.owner.id")
    List<Object[]> sumAmountByOwnerIdsAndType(@Param("type") Wallet.TransactionType type,
                                              @Param("ids") Collection<Long> ids);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM WalletTransaction t " +
            "WHERE t.type = :type AND t.dummy = false AND t.deleted = false")
    BigDecimal sumByTypeExcludingDummy(@Param("type") Wallet.TransactionType type);
}