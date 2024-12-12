package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.CreateDriverApplicationResponseDTO;
import edu.zut.bookrider.dto.CreateDriverDocumentDTO;
import edu.zut.bookrider.dto.CreateDriverDocumentStringDTO;
import edu.zut.bookrider.service.DriverApplicationRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/driver-applications")
public class DriverApplicationController {

    private final DriverApplicationRequestService driverApplicationRequestService;

    @Secured("driver")
    @PostMapping
    public ResponseEntity<?> createDriverApplication(
            @RequestBody List<@Valid CreateDriverDocumentStringDTO> files,
            Authentication authentication
    ) {
        List<CreateDriverDocumentDTO> documents = new ArrayList<>();

        for (CreateDriverDocumentStringDTO documentDTO : files) {
            byte[] decodedBytes = Base64.getDecoder().decode(documentDTO.getBase64Image());
            String documentType = documentDTO.getDocumentType();
            LocalDate expirationDate = LocalDate.parse(documentDTO.getExpirationDate(), DateTimeFormatter.ISO_DATE);

            CreateDriverDocumentDTO document = new CreateDriverDocumentDTO();
            document.setImageInBytes(decodedBytes);
            document.setDocumentType(documentType);
            document.setExpiryDate(expirationDate);

            documents.add(document);
        }

        CreateDriverApplicationResponseDTO responseDTO = driverApplicationRequestService.createDriverApplication(authentication, documents);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
