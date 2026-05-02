package com.suraj.Customer_Portal_29.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfMergerService {

    public byte[] mergePdfs(List<byte[]> pdfBytesList) throws IOException {

        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        merger.setDestinationStream(outputStream);

        for (byte[] pdf : pdfBytesList) {
            merger.addSource(new ByteArrayInputStream(pdf));
        }

        merger.mergeDocuments(
                org.apache.pdfbox.io.MemoryUsageSetting.setupMainMemoryOnly()
        );

        return outputStream.toByteArray();
    }
}