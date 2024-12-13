package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CreateDriverApplicationResponseDTO;
import edu.zut.bookrider.dto.CreateDriverDocumentDTO;
import edu.zut.bookrider.dto.CreateDriverDocumentResponseDTO;
import edu.zut.bookrider.model.DriverApplicationRequest;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import edu.zut.bookrider.repository.DriverApplicationRequestRepository;
import edu.zut.bookrider.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DriverApplicationRequestService {

    private final UserRepository userRepository;
    private final DriverApplicationRequestRepository driverApplicationRequestRepository;
    private final DriverDocumentService driverDocumentService;

    @Transactional
    public CreateDriverApplicationResponseDTO createDriverApplication(
            Authentication authentication,
            List<CreateDriverDocumentDTO> documents) {

        String driverEmail = authentication.getName().split(":")[0];

        User driver = userRepository.findByEmailAndRoleName(driverEmail, "driver")
                .orElseThrow(() -> new IllegalArgumentException("User with the provided email and 'driver' role doesn't exist"));

        if (driverApplicationRequestRepository.existsByUserIdAndPendingOrUnderReview(driver.getId())) {
            throw new IllegalStateException("The user already has a pending or under-review driver application request.");
        }

        DriverApplicationRequest applicationRequest = new DriverApplicationRequest();
        applicationRequest.setUser(driver);
        applicationRequest.setStatus(DriverApplicationStatus.PENDING);
        DriverApplicationRequest savedRequest = driverApplicationRequestRepository.save(applicationRequest);

        List<CreateDriverDocumentResponseDTO> createdDocuments = new ArrayList<>();

        for (CreateDriverDocumentDTO documentDTO : documents) {
            try {
                CreateDriverDocumentResponseDTO createdDocument = driverDocumentService.saveDriverDocument(
                        documentDTO,
                        savedRequest
                );
                createdDocuments.add(createdDocument);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new CreateDriverApplicationResponseDTO(
                savedRequest.getId(),
                savedRequest.getUser().getEmail(),
                savedRequest.getStatus().toString(),
                createdDocuments
        );
    }
}
