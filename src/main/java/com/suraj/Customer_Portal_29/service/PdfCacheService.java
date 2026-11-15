package com.suraj.Customer_Portal_29.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PdfCacheService {

    private static final long TTL_MS = 5 * 60 * 1000L; // 5 minutes

    // key: data:{recordId}:{includeSignatures}
    private final ConcurrentHashMap<String, Entry<Map<String, Object>>> dataCache = new ConcurrentHashMap<>();
    // key: pdf:{recordId}:{type}:{includeSignatures}
    private final ConcurrentHashMap<String, Entry<byte[]>> pdfCache = new ConcurrentHashMap<>();
    // key: word:{recordId}:{type}:{includeSignatures}
    private final ConcurrentHashMap<String, Entry<byte[]>> wordCache = new ConcurrentHashMap<>();

    public record Entry<T>(T value, long expiresAt) {}

    // ---------------- DATA CACHE ----------------

    @SuppressWarnings("unchecked")
    public Map<String, Object> getData(String recordId, boolean includeSignatures) {
        String key = "data:" + recordId + ":" + includeSignatures;
        Entry<?> e = dataCache.get(key);
        if (e == null || e.expiresAt() < System.currentTimeMillis()) {
            dataCache.remove(key);
            return null;
        }
        return (Map<String, Object>) e.value();
    }

    public void putData(String recordId, boolean includeSignatures, Map<String, Object> data) {
        String key = "data:" + recordId + ":" + includeSignatures;
        dataCache.put(key, new Entry<>(data, System.currentTimeMillis() + TTL_MS));
    }

    // ---------------- PDF CACHE ----------------

    public byte[] getPdf(String recordId, String type, boolean includeSignatures) {
        String key = "pdf:" + recordId + ":" + type + ":" + includeSignatures;
        Entry<byte[]> e = pdfCache.get(key);
        if (e == null || e.expiresAt() < System.currentTimeMillis()) {
            pdfCache.remove(key);
            return null;
        }
        return e.value();
    }

    public void putPdf(String recordId, String type, boolean includeSignatures, byte[] bytes) {
        String key = "pdf:" + recordId + ":" + type + ":" + includeSignatures;
        pdfCache.put(key, new Entry<>(bytes, System.currentTimeMillis() + TTL_MS));
    }

    // ---------------- WORD CACHE ----------------

    public byte[] getWord(String recordId, String type, boolean includeSignatures) {
        String key = "word:" + recordId + ":" + type + ":" + includeSignatures;
        Entry<byte[]> e = wordCache.get(key);
        if (e == null || e.expiresAt() < System.currentTimeMillis()) {
            wordCache.remove(key);
            return null;
        }
        return e.value();
    }

    public void putWord(String recordId, String type, boolean includeSignatures, byte[] bytes) {
        String key = "word:" + recordId + ":" + type + ":" + includeSignatures;
        wordCache.put(key, new Entry<>(bytes, System.currentTimeMillis() + TTL_MS));
    }

    // ---------------- EVICT ----------------

    /** Call this whenever a record is updated or deleted. */
    public void evictRecord(String recordId) {
        if (recordId == null) return;
        dataCache.keySet().removeIf(k -> k.contains(":" + recordId + ":"));
        pdfCache.keySet().removeIf(k -> k.contains(":" + recordId + ":"));
        wordCache.keySet().removeIf(k -> k.contains(":" + recordId + ":"));
    }

    /** Sweep expired entries every 60 seconds. */
    @Scheduled(fixedRate = 60_000)
    public void sweepExpired() {
        long now = System.currentTimeMillis();
        dataCache.entrySet().removeIf(en -> en.getValue().expiresAt() < now);
        pdfCache.entrySet().removeIf(en -> en.getValue().expiresAt() < now);
        wordCache.entrySet().removeIf(en -> en.getValue().expiresAt() < now);
    }
}