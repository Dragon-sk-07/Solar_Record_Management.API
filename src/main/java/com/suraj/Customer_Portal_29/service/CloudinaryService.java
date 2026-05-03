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

    private static final int MAX_WIDTH = 500;
    private static final int MAX_HEIGHT = 500;
    private static final long MAX_TARGET_SIZE = 150 * 1024; // 150KB

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file, String folderName) {
        try {
            if (file.getSize() > 2 * 1024 * 1024) {
                throw new RuntimeException("File too large. Max 2MB allowed.");
            }

            byte[] compressedImage = compressImageSafely(file.getBytes());
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
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    private byte[] compressImageSafely(byte[] original) throws IOException {
        ByteArrayInputStream bais = null;
        BufferedImage image = null;

        try {
            bais = new ByteArrayInputStream(original);
            image = ImageIO.read(bais);

            if (image == null) {
                return original;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                float widthRatio = (float) MAX_WIDTH / width;
                float heightRatio = (float) MAX_HEIGHT / height;
                float ratio = Math.min(widthRatio, heightRatio);
                width = (int) (width * ratio);
                height = (int) (height * ratio);
                if (width < 1) width = 1;
                if (height < 1) height = 1;
            }

            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = resized.createGraphics();
            try {
                g.drawImage(image, 0, 0, width, height, null);
            } finally {
                g.dispose();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", baos);

            byte[] result = baos.toByteArray();
            resized.flush();

            if (result.length > MAX_TARGET_SIZE) {
                return compressWithHigherQuality(resized);
            }

            return result;

        } finally {
            if (bais != null) {
                bais.close();
            }
            if (image != null) {
                image.flush();
            }
        }
    }

    private byte[] compressWithHigherQuality(BufferedImage image) throws IOException {
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
        param.setCompressionQuality(0.6f);

        try {
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        byte[] result = baos.toByteArray();

        if (result.length > MAX_TARGET_SIZE) {
            ByteArrayOutputStream fallback = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", fallback);
            return fallback.toByteArray();
        }

        return result;
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