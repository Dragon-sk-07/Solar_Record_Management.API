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

        try {
            // Save each PDF to a temp file (PDFBox 2.0.27 requires File, not InputStream)
            for (int i = 0; i < pdfBytesList.size(); i++) {
                byte[] pdf = pdfBytesList.get(i);
                if (pdf != null && pdf.length > 0) {
                    File tempFile = File.createTempFile("pdf_merge_" + i + "_", ".pdf");
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(pdf);
                    }
                    merger.addSource(tempFile);
                    tempFiles.add(tempFile);
                }
            }

            // Merge documents - no parameter needed for PDFBox 2.0.27
            merger.mergeDocuments();

            byte[] result = outputStream.toByteArray();

            // Clean up temp files
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }

            return result;

        } catch (Exception e) {
            // Clean up on error
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
            throw new IOException("Failed to merge PDFs: " + e.getMessage(), e);
        }
    }
}