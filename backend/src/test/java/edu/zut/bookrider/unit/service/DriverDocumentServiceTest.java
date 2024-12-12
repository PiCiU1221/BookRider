package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.CreateDriverDocumentDTO;
import edu.zut.bookrider.dto.CreateDriverDocumentResponseDTO;
import edu.zut.bookrider.model.DriverApplicationRequest;
import edu.zut.bookrider.model.DriverDocument;
import edu.zut.bookrider.repository.DriverDocumentRepository;
import edu.zut.bookrider.service.DriverDocumentService;
import edu.zut.bookrider.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class DriverDocumentServiceTest {

    @InjectMocks
    private DriverDocumentService  driverDocumentService;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private DriverDocumentRepository driverDocumentRepository;

    private DriverApplicationRequest applicationRequest;
    private CreateDriverDocumentDTO documentDto;

    @BeforeEach
    void setUp() {
        applicationRequest = new DriverApplicationRequest();
        applicationRequest.setId(1);

        documentDto = new CreateDriverDocumentDTO();
        documentDto.setDocumentType("License");
        documentDto.setExpiryDate(LocalDate.now().plusYears(5));
        documentDto.setImageInBytes(new byte[0]);
    }

    @Test
    void whenValidInputData_thenReturnCreatedDTO() throws IOException {
        String documentUrl = "http://example.com/license.jpg";
        MultipartFile multipartFile = new MockMultipartFile("licence.jpg", documentDto.getImageInBytes());
        when(imageUploadService.uploadImage(multipartFile)).thenReturn(documentUrl);

        DriverDocument driverDocument = new DriverDocument();
        driverDocument.setDriverApplicationRequest(applicationRequest);
        driverDocument.setDocumentType(documentDto.getDocumentType());
        driverDocument.setDocumentPhotoUrl(documentUrl);
        driverDocument.setExpiryDate(documentDto.getExpiryDate());

        when(driverDocumentRepository.save(any(DriverDocument.class))).thenReturn(driverDocument);

        CreateDriverDocumentResponseDTO response = driverDocumentService.saveDriverDocument(documentDto, applicationRequest);

        assertEquals("License", response.getDocumentType());
        assertEquals(documentUrl, response.getDocumentPhotoUrl());
        assertEquals(LocalDate.now().plusYears(5), response.getExpiryDate());
    }

    @Test
    void whenInvalidImageInRequest_thenThrowException() throws IOException {
        when(imageUploadService.uploadImage(any())).thenThrow(new IOException());

        assertThrows(IOException.class, () -> {
            driverDocumentService.saveDriverDocument(documentDto, applicationRequest);
        });
    }
}
