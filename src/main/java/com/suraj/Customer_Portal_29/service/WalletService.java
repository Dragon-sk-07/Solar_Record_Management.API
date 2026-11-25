package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.response.WalletResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.PdfDocType;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.entity.Wallet;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.repository.WalletRepository;
import com.suraj.Customer_Portal_29.repository.WalletTransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository txRepository;
    private final OwnerRepository ownerRepository;

    public WalletService(WalletRepository walletRepository,
                         WalletTransactionRepository txRepository,
                         OwnerRepository ownerRepository) {
        this.walletRepository = walletRepository;
        this.txRepository = txRepository;
        this.ownerRepository = ownerRepository;
    }

    private Owner getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ownerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public Wallet ensureWallet(Owner owner) {
        return walletRepository.findByOwnerId(owner.getId())
                .orElseGet(() -> {
                    Wallet w = new Wallet();
                    w.setOwner(owner);
                    w.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(w);
                });
    }

    @Transactional(readOnly = true)
    public WalletResponseDto getMyWallet() {
        Owner owner = getCurrentUser();
        Wallet wallet = walletRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal displayBalance = wallet.getBalance();

        if (owner.getRole() == UserRole.SUPER_ADMIN) {
            displayBalance = calculateSystemRevenue();
        }

        return WalletResponseDto.builder()
                .walletId(wallet.getId())
                .ownerId(owner.getId())
                .ownerEmail(owner.getEmail())
                .balance(displayBalance)
                .updatedAt(wallet.getUpdatedAt())
                .pricing(pricingList())
                .build();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateSystemRevenue() {
        BigDecimal debits = txRepository.sumByTypeExcludingDummy(Wallet.TransactionType.DEBIT);
        if (debits == null) debits = BigDecimal.ZERO;
        return debits;
    }

    private static final BigDecimal DUMMY_FREE_CREDITS = new BigDecimal("100.00");

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Wallet creditDummyStarter(Long ownerId, String performedByEmail) {
        Wallet wallet = walletRepository.findByOwnerIdForUpdate(ownerId)
                .orElseGet(() -> {
                    Owner owner = ownerRepository.findById(ownerId)
                            .orElseThrow(() -> new RuntimeException("Owner not found: " + ownerId));
                    Wallet w = new Wallet();
                    w.setOwner(owner);
                    w.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(w);
                });

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(DUMMY_FREE_CREDITS);
        wallet.setBalance(after);
        walletRepository.save(wallet);

        Owner owner = wallet.getOwner();
        owner.setWalletBalance(after);
        ownerRepository.save(owner);

        Wallet.Transaction tx = new Wallet.Transaction();
        tx.setWallet(wallet);
        tx.setType(Wallet.TransactionType.CREDIT);
        tx.setAmount(DUMMY_FREE_CREDITS);
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        tx.setNote("Demo user starter credits (free)");
        tx.setPerformedByEmail(performedByEmail);
        tx.setDummy(true);
        txRepository.save(tx);

        return wallet;
    }

    @Transactional(readOnly = true)
    public List<WalletResponseDto.TransactionItem> getMyTransactions(int page, int size) {
        Owner owner = getCurrentUser();
        Wallet wallet = ensureWallet(owner);
        return txRepository
                .findByWalletIdAndDeletedFalseOrderByCreatedAtDesc(
                        wallet.getId(), PageRequest.of(page, size))
                .stream()
                .map(WalletResponseDto::toTransactionItem)
                .collect(Collectors.toList());
    }

    public List<WalletResponseDto.PricingItem> pricingList() {
        return Arrays.stream(PdfDocType.values())
                .map(WalletResponseDto::toPricingItem)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Wallet credit(Long ownerId, BigDecimal amount, String note, String performedByEmail) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }

        Wallet wallet = walletRepository.findByOwnerIdForUpdate(ownerId)
                .orElseGet(() -> {
                    Owner owner = ownerRepository.findById(ownerId)
                            .orElseThrow(() -> new RuntimeException("Owner not found: " + ownerId));
                    Wallet w = new Wallet();
                    w.setOwner(owner);
                    w.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(w);
                });

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(amount);
        wallet.setBalance(after);
        walletRepository.save(wallet);

        Owner owner = wallet.getOwner();
        if (owner.isDummy()) {
            throw new IllegalStateException("Dummy users have fixed demo credits and cannot be topped up.");
        }
        owner.setWalletBalance(after);
        ownerRepository.save(owner);

        Wallet.Transaction tx = new Wallet.Transaction();
        tx.setWallet(wallet);
        tx.setType(Wallet.TransactionType.CREDIT);
        tx.setAmount(amount);
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        tx.setNote(note);
        tx.setPerformedByEmail(performedByEmail);
        txRepository.save(tx);

        return wallet;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Wallet debit(Long ownerId, BigDecimal amount, PdfDocType docType,
                        String solarRecordId, String previewToken, String performedByEmail) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (docType == null) {
            throw new IllegalArgumentException("Doc type is required for debit");
        }

        Wallet wallet = walletRepository.findByOwnerIdForUpdate(ownerId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Owner owner = wallet.getOwner();
        if (owner.getRole() == UserRole.SUPER_ADMIN) {
            return wallet;
        }
        if (owner.isDummy()) {
            throw new IllegalStateException("Dummy users cannot be credited or reset.");
        }

        BigDecimal before = wallet.getBalance();
        if (before.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient wallet balance. Required " + amount + ", available " + before);
        }

        if (previewToken != null && txRepository.existsByPreviewTokenAndDeletedFalse(previewToken)) {
            throw new IllegalStateException("Duplicate download detected");
        }

        BigDecimal after = before.subtract(amount);
        wallet.setBalance(after);
        walletRepository.save(wallet);

        owner.setWalletBalance(after);
        ownerRepository.save(owner);

        Wallet.Transaction tx = new Wallet.Transaction();
        tx.setWallet(wallet);
        tx.setType(Wallet.TransactionType.DEBIT);
        tx.setAmount(amount);
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        tx.setDocType(docType);
        tx.setSolarRecordId(solarRecordId);
        tx.setPreviewToken(previewToken);
        tx.setPerformedByEmail(performedByEmail);
        tx.setDummy(owner.isDummy());
        txRepository.save(tx);

        return wallet;
    }

    @Transactional(readOnly = true)
    public List<WalletResponseDto.TransactionItem> getAllTransactions() {
        return txRepository.findAllByDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(WalletResponseDto::toTransactionItem)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAddedForUser(Long ownerId) {
        return txRepository.sumAmountByOwnerIdAndType(
                ownerId, Wallet.TransactionType.CREDIT);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getCreditTotalsByOwnerIds(java.util.Collection<Long> ownerIds) {
        if (ownerIds == null || ownerIds.isEmpty()) {
            return List.of();
        }
        return txRepository.sumAmountByOwnerIdsAndType(
                Wallet.TransactionType.CREDIT, ownerIds);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void resetUserWallet(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + ownerId));

        if (owner.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot reset SUPER_ADMIN wallet");
        }
        if (owner.isDummy()) {
            throw new IllegalStateException("Dummy users have fixed demo credits and cannot be reset.");
        }

        Wallet wallet = walletRepository.findByOwnerIdForUpdate(ownerId)
                .orElseGet(() -> {
                    Wallet w = new Wallet();
                    w.setOwner(owner);
                    w.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(w);
                });

        txRepository.softDeleteByWalletId(wallet.getId());

        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        owner.setWalletBalance(BigDecimal.ZERO);
        ownerRepository.save(owner);
    }
}