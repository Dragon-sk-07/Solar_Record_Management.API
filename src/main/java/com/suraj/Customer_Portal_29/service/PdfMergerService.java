package com.suraj.Customer_Portal_29.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfMergerService {

    public byte[] mergePdfs(List<byte[]> pdfBytesList) throws IOException {
        if (pdfBytesList == null || pdfBytesList.isEmpty()) {
            throw new IOException("No PDFs to merge");
        }

        // Use temp file mode to save memory
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);

        // Add all sources
        for (byte[] pdf : pdfBytesList) {
            if (pdf != null && pdf.length > 0) {
                merger.addSource(new ByteArrayInputStream(pdf));
            }
        }

        // Merge using temp files (lower memory usage)
        merger.mergeDocuments(
                org.apache.pdfbox.io.MemoryUsageSetting.setupTempFileOnly()
        );

        return outputStream.toByteArray();
    }
}