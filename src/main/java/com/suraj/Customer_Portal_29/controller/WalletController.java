package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.response.ApiResponseDto;
import com.suraj.Customer_Portal_29.dto.response.WalletResponseDto;
import com.suraj.Customer_Portal_29.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ApiResponseDto<WalletResponseDto> getMyWallet() {
        return new ApiResponseDto<>("Wallet fetched successfully",
                walletService.getMyWallet());
    }

    @GetMapping("/me/transactions")
    public ApiResponseDto<List<WalletResponseDto.TransactionItem>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return new ApiResponseDto<>("Transactions fetched successfully",
                walletService.getMyTransactions(page, size));
    }

    @GetMapping("/pricing")
    public ApiResponseDto<List<WalletResponseDto.PricingItem>> getPricing() {
        return new ApiResponseDto<>("Pricing fetched successfully",
                walletService.pricingList());
    }

    @GetMapping("/admin/transactions")
    public ApiResponseDto<List<WalletResponseDto.TransactionItem>> getAllTransactions() {
        return new ApiResponseDto<>("All transactions fetched successfully",
                walletService.getAllTransactions());
    }

    @PostMapping("/admin/users/{userId}/reset")
    public ResponseEntity<Map<String, String>> resetUserWallet(@PathVariable Long userId) {
        walletService.resetUserWallet(userId);
        return ResponseEntity.ok(Map.of("message", "Wallet reset to zero successfully"));
    }
}