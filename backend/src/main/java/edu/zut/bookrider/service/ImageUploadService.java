package edu.zut.bookrider.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.exception.ImageProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

@RequiredArgsConstructor
@Service
public class ImageUploadService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_DIMENSION = 1600;
    private static final float JPG_QUALITY = 0.8f;

    public String uploadImage(MultipartFile imageFile) {
        try {
            byte[] resizedBytes = resizeAndCompress(imageFile.getBytes());

            String base64Image = Base64.getEncoder().encodeToString(resizedBytes);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // key is public, no need to hide it
            String apiKey = "6d207e02198a847aa98d0a2a901485a5";

            body.add("key", apiKey);
            body.add("action", "upload");
            body.add("source", base64Image);
            body.add("format", "json");

            String apiUrl = "https://freeimage.host/api/1/upload";

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    apiUrl,
                    new HttpEntity<>(body),
                    String.class
            );

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to upload image: " + responseEntity.getStatusCode());
            }

            return extractImageUrlFromResponse(responseEntity.getBody());
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to process uploaded image: " + e.getMessage());
        }
    }

    private String extractImageUrlFromResponse(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.path("image").path("url").asText();
    }

    private BufferedImage createOptimizedImage(BufferedImage src, int targetWidth, int targetHeight) {
        BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return img;
    }

    private BufferedImage convertToRGB(BufferedImage img) {
        if (img.getType() == BufferedImage.TYPE_INT_RGB) {
            return img;
        }
        return createOptimizedImage(img, img.getWidth(), img.getHeight());
    }


    private BufferedImage resizeImage(BufferedImage img) {
        float scale = (float) MAX_DIMENSION / Math.max(img.getWidth(), img.getHeight());
        int newWidth = Math.round(img.getWidth() * scale);
        int newHeight = Math.round(img.getHeight() * scale);

        return createOptimizedImage(img, newWidth, newHeight);
    }

    private byte[] encodeJpg(BufferedImage img) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPG writers available");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(JPG_QUALITY);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }

        return out.toByteArray();
    }

    private byte[] resizeAndCompress(byte[] inputBytes) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(inputBytes));
        if (original == null) {
            throw new IOException("Invalid image data");
        }

        BufferedImage processed;
        int maxSide = Math.max(original.getWidth(), original.getHeight());

        if (maxSide > MAX_DIMENSION) {
            processed = resizeImage(original);
        } else {
            processed = convertToRGB(original);
        }

        return encodeJpg(processed);
    }
}
