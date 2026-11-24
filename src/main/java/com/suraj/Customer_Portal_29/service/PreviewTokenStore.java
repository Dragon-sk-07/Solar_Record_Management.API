package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.entity.PdfDocType;
import com.suraj.Customer_Portal_29.entity.PreviewToken;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PreviewTokenStore {

    private static final int TTL_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, PreviewToken> cache = new ConcurrentHashMap<>();

    public PreviewToken create(String ownerEmail, String solarRecordId,
                               PdfDocType docType, byte[] pdfBytes) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = HexFormat.of().formatHex(raw);

        PreviewToken pt = new PreviewToken(
                token, ownerEmail, solarRecordId, docType, pdfBytes,
                LocalDateTime.now().plusMinutes(TTL_MINUTES));
        cache.put(token, pt);
        return pt;
    }

    public PreviewToken consume(String token, String ownerEmail) {
        PreviewToken pt = cache.get(token);
        if (pt == null) return null;
        if (pt.isExpired()) { cache.remove(token); return null; }
        if (!pt.getOwnerEmail().equalsIgnoreCase(ownerEmail)) return null;
        return pt;
    }

    public void remove(String token) {
        if (token != null) cache.remove(token);
    }

    @Scheduled(fixedRate = 60_000)
    public void purgeExpired() {
        cache.entrySet().removeIf(e -> e.getValue().isExpired());
    }
}