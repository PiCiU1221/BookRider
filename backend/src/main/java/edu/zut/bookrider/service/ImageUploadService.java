package edu.zut.bookrider.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public String uploadImage(MultipartFile imageFile) throws IOException {
        // Compress the image
        byte[] compressedImageBytes = compressImage(imageFile.getBytes());

        // Encode the compressed image bytes to base64
        String base64Image = Base64.getEncoder().encodeToString(compressedImageBytes);

        // Prepare the request body
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

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return extractImageUrlFromResponse(responseEntity.getBody());
        } else {
            throw new RuntimeException("Failed to upload image: " + responseEntity.getStatusCode());
        }
    }

    private String extractImageUrlFromResponse(String responseBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode.path("image").path("url").asText();
    }

    private byte[] compressImage(byte[] originalImageBytes) throws IOException {
        // Convert the original image bytes to BufferedImage
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageBytes));

        // Set the compression quality based on the original image size
        float compressionQuality = calculateCompressionQuality(originalImageBytes.length);

        // Create a ByteArrayOutputStream to hold the compressed image bytes
        ByteArrayOutputStream compressedImageOutputStream = new ByteArrayOutputStream();

        // Get all available ImageWriters
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (writers.hasNext()) {
            ImageWriter writer = writers.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();

            // Set compression quality
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(compressionQuality);

            // Write the compressed image bytes to the ByteArrayOutputStream
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(compressedImageOutputStream)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(originalImage, null, null), writeParam);
            } finally {
                writer.dispose();
            }
        }

        return compressedImageOutputStream.toByteArray();
    }

    private float calculateCompressionQuality(int originalImageSize) {
        // Set a threshold for image size above which we apply more compression
        int sizeThreshold = 10 * 1024 * 1024; // 10 MB

        // Set the initial compression quality
        float initialCompressionQuality = 0.8f;

        // Adjust compression quality based on image size
        if (originalImageSize > sizeThreshold) {
            // If the original image is larger than 10MB, increase compression to reduce size
            return 0.5f;
        } else {
            // Otherwise, use the initial compression quality
            return initialCompressionQuality;
        }
    }
}
