package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.service.ImageUploadService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ImageUploadServiceTest {

    @Autowired
    private ImageUploadService imageUploadService;

    @MockBean
    private RestTemplate restTemplate;

    private static String validResponseJson;

    @BeforeAll
    static void setUp() throws IOException {
        validResponseJson = new String(Files.readAllBytes(Paths.get("src/test/resources/imageUploadServiceTest/valid-api-response.json")));
    }

    @Test
    void whenValidImage_thenReturnUploadedImageUrl() throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/imageUploadServiceTest/example_image.jpg"));

        MultipartFile multipartFile = new MockMultipartFile(
                "imageFile",
                "example_image.jpg",
                "image/jpeg",
                imageBytes
        );

        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(validResponseJson));

        String result = assertDoesNotThrow(() -> imageUploadService.uploadImage(multipartFile));
        assertNotNull(result);
        assertEquals("https://iili.io/2GXfb8N.jpg", result);
    }
}
