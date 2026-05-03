package com.suraj.Customer_Portal_29.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Iterator;

@Service
public class CloudinaryService {

    private static final int TARGET_WIDTH = 800;
    private static final long MAX_SIZE_BYTES = 500 * 1024;

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file, String folderName) {
        try {
            byte[] compressedImage = compressImage(file.getBytes());
            MultipartFile compressedFile = new CompressedMultipartFile(
                    compressedImage,
                    file.getOriginalFilename(),
                    file.getContentType()
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    compressedFile.getBytes(),
                    ObjectUtils.asMap("folder", folderName)
            );
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    private byte[] compressImage(byte[] original) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(original);
        BufferedImage image = ImageIO.read(bais);

        if (image == null) {
            return original;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width > TARGET_WIDTH) {
            float ratio = (float) TARGET_WIDTH / width;
            width = TARGET_WIDTH;
            height = (int) (height * ratio);
        }

        java.awt.Image scaled = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        resized.getGraphics().drawImage(scaled, 0, 0, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", baos);

        byte[] result = baos.toByteArray();

        if (result.length > MAX_SIZE_BYTES) {
            return compressWithQuality(resized);
        }

        return result;
    }

    private byte[] compressWithQuality(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ByteArrayOutputStream fallback = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", fallback);
            return fallback.toByteArray();
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.7f);

        writer.setOutput(ImageIO.createImageOutputStream(baos));
        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();

        return baos.toByteArray();
    }

    private static class CompressedMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String filename;
        private final String contentType;

        public CompressedMultipartFile(byte[] content, String filename, String contentType) {
            this.content = content;
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}