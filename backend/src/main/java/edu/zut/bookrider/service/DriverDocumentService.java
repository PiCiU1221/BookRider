package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CreateDriverDocumentDTO;
import edu.zut.bookrider.dto.CreateDriverDocumentResponseDTO;
import edu.zut.bookrider.model.DriverApplicationRequest;
import edu.zut.bookrider.model.DriverDocument;
import edu.zut.bookrider.repository.DriverDocumentRepository;
import edu.zut.bookrider.util.BASE64DecodedMultipartFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class DriverDocumentService {

    private final ImageUploadService imageUploadService;
    private final DriverDocumentRepository driverDocumentRepository;

    public CreateDriverDocumentResponseDTO saveDriverDocument(
            @Valid CreateDriverDocumentDTO documentDto,
            DriverApplicationRequest applicationRequest
    ) {
        MultipartFile multipartFile = new BASE64DecodedMultipartFile(documentDto.getImageInBytes());

        String documentUrl = imageUploadService.uploadImage(multipartFile);

        DriverDocument driverDocument = new DriverDocument();
        driverDocument.setDriverApplicationRequest(applicationRequest);
        driverDocument.setDocumentType(documentDto.getDocumentType());
        driverDocument.setDocumentPhotoUrl(documentUrl);
        driverDocument.setExpiryDate(documentDto.getExpiryDate());

        DriverDocument savedDocument = driverDocumentRepository.save(driverDocument);

        return new CreateDriverDocumentResponseDTO(
                savedDocument.getDocumentType(),
                savedDocument.getDocumentPhotoUrl(),
                savedDocument.getExpiryDate()
        );
    }
}
