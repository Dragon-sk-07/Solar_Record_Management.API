package com.suraj.Customer_Portal_29.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfMergerService {

    public byte[] mergePdfs(List<byte[]> pdfBytesList) throws IOException {
        if (pdfBytesList == null || pdfBytesList.isEmpty()) {
            throw new IOException("No PDFs to merge");
        }

        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);

        List<File> tempFiles = new ArrayList<>();
        int validPdfCount = 0;

        try {
            for (int i = 0; i < pdfBytesList.size(); i++) {
                byte[] pdf = pdfBytesList.get(i);
                // Check if PDF is valid (not null, at least 1KB)
                if (pdf != null && pdf.length > 1024) {
                    File tempFile = File.createTempFile("pdf_" + i + "_", ".pdf");
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(pdf);
                    }
                    merger.addSource(tempFile);
                    tempFiles.add(tempFile);
                    validPdfCount++;
                    System.out.println("Added PDF " + i + " to merge, size: " + pdf.length + " bytes");
                } else {
                    System.err.println("Skipping invalid PDF at index " + i + ", size: " + (pdf != null ? pdf.length : "null"));
                }
            }

            if (tempFiles.isEmpty()) {
                throw new IOException("No valid PDF files to merge. Total received: " + pdfBytesList.size());
            }

            System.out.println("Merging " + validPdfCount + " valid PDFs");
            merger.mergeDocuments();

            byte[] result = outputStream.toByteArray();
            System.out.println("Merge complete. Output size: " + result.length + " bytes");
            return result;

        } finally {
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
    }
}