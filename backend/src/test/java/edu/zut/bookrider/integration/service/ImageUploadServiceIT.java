package edu.zut.bookrider.integration.service;

import edu.zut.bookrider.service.ImageUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ImageUploadServiceIT {

    @Autowired
    private ImageUploadService imageUploadService;

    @Test
    void whenValidImage_thenDontThrowException() throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test_image.jpg"));

        // We have to mock it, since it's an interface populated by
        // Spring during an HTTP multipart request in a real application
        MultipartFile multipartFile = new MockMultipartFile(
                "imageFile",
                "example_image.jpg",
                "image/jpeg",
                imageBytes
        );

        String result = assertDoesNotThrow(() -> imageUploadService.uploadImage(multipartFile));
        assertNotNull(result);

        // To check the link manually
        // System.out.println(result);
    }
}
