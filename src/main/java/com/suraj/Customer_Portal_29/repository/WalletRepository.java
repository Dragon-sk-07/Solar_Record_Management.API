package com.suraj.Customer_Portal_29.repository;

import com.suraj.Customer_Portal_29.entity.Wallet;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByOwnerId(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.owner.id = :ownerId")
    Optional<Wallet> findByOwnerIdForUpdate(@Param("ownerId") Long ownerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Wallet w WHERE w.owner.id = :ownerId")
    int deleteByOwnerId(@Param("ownerId") Long ownerId);

    <S extends Wallet.Transaction> S save(S transaction);

    @Query("SELECT t FROM WalletTransaction t WHERE t.wallet.id = :walletId AND t.deleted = false ORDER BY t.createdAt DESC")
    Page<Wallet.Transaction> findByWalletIdAndDeletedFalseOrderByCreatedAtDesc(
            @Param("walletId") Long walletId, Pageable pageable);

    @Query("SELECT t FROM WalletTransaction t WHERE t.wallet.id = :walletId AND t.deleted = false ORDER BY t.createdAt DESC")
    List<Wallet.Transaction> findTop50ByWalletIdAndDeletedFalseOrderByCreatedAtDesc(@Param("walletId") Long walletId);

    @Query("SELECT t FROM WalletTransaction t WHERE t.deleted = false ORDER BY t.createdAt DESC")
    List<Wallet.Transaction> findAllByDeletedFalseOrderByCreatedAtDesc();

    @Query("SELECT COUNT(t) > 0 FROM WalletTransaction t WHERE t.previewToken = :previewToken AND t.deleted = false")
    boolean existsByPreviewTokenAndDeletedFalse(@Param("previewToken") String previewToken);

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

    @Modifying
    @Transactional
    @Query("DELETE FROM WalletTransaction t WHERE t.wallet.owner.id = :ownerId")
    int deleteByWalletOwnerId(@Param("ownerId") Long ownerId);
}