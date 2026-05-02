package com.suraj.Customer_Portal_29.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfMergerService {

    public byte[] mergePdfs(List<byte[]> pdfBytesList) throws IOException {
        try (PDDocument mergedDocument = new PDDocument()) {
            for (byte[] pdfBytes : pdfBytesList) {
                try (PDDocument doc = PDDocument.load(pdfBytes)) {
                    for (int i = 0; i < doc.getNumberOfPages(); i++) {
                        mergedDocument.addPage(doc.getPage(i));
                    }
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mergedDocument.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}