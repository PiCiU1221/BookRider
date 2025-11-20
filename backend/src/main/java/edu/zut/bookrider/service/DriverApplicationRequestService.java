package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.exception.DriverApplicationNotFoundException;
import edu.zut.bookrider.exception.InvalidDocumentTypeException;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.mapper.driverApplication.DriverApplicationReadMapper;
import edu.zut.bookrider.model.DriverApplicationRequest;
import edu.zut.bookrider.model.DriverDocument;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.DocumentType;
import edu.zut.bookrider.model.enums.DriverApplicationStatus;
import edu.zut.bookrider.repository.DriverApplicationRequestRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.websocket.WebSocketHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DriverApplicationRequestService {

    private final UserRepository userRepository;
    private final DriverApplicationRequestRepository driverApplicationRequestRepository;
    private final DriverDocumentService driverDocumentService;
    private final DriverApplicationReadMapper driverApplicationReadMapper;
    private final UserService userService;

    private final WebSocketHandler webSocketHandler;

    @Transactional
    public CreateDriverApplicationResponseDTO processDriverApplication(
            Authentication authentication,
            List<CreateDriverDocumentStringDTO> files) {

        List<CreateDriverDocumentDTO> documents = files.stream().map(documentDTO -> {
            byte[] decodedBytes = Base64.getDecoder().decode(documentDTO.getBase64Image());

            DocumentType documentType;
            try {
                documentType = DocumentType.valueOf(documentDTO.getDocumentType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidDocumentTypeException("Invalid document type: " + documentDTO.getDocumentType());
            }

            LocalDate expirationDate = LocalDate.parse(documentDTO.getExpirationDate(), DateTimeFormatter.ISO_DATE);

            CreateDriverDocumentDTO document = new CreateDriverDocumentDTO();
            document.setImageInBytes(decodedBytes);
            document.setDocumentType(documentType);
            document.setExpiryDate(expirationDate);

            return document;
        }).collect(Collectors.toList());

        return createDriverApplication(authentication, documents);
    }

    @Transactional
    public CreateDriverApplicationResponseDTO createDriverApplication(
            Authentication authentication,
            List<@Valid CreateDriverDocumentDTO> documents) {

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
            CreateDriverDocumentResponseDTO createdDocument = driverDocumentService.saveDriverDocument(
                    documentDTO,
                    savedRequest
            );
            createdDocuments.add(createdDocument);
        }

        List<User> administrators = userService.getAllAdministrators();
        for (User administrator : administrators) {
            webSocketHandler.sendRefreshSignal(administrator.getEmail(), "administrator/driver-applications");
        }

        return new CreateDriverApplicationResponseDTO(
                savedRequest.getId(),
                savedRequest.getUser().getEmail(),
                savedRequest.getStatus().toString(),
                createdDocuments
        );
    }

    public List<DriverApplicationSummaryDTO> listApplications(
            List<DriverApplicationStatus> statuses,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "submittedAt"));
        Page<DriverApplicationRequest> applicationsPage;

        if (statuses == null || statuses.isEmpty()) {
            applicationsPage = driverApplicationRequestRepository.findAll(pageable);
        } else {
            applicationsPage = driverApplicationRequestRepository.findByStatusIn(statuses, pageable);
        }

        return applicationsPage.getContent().stream()
                .map(driverApplicationReadMapper::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverApplicationDetailsDTO getApplicationDetails(
            Integer id,
            Authentication authentication) {

        DriverApplicationRequest application = driverApplicationRequestRepository.findById(id)
                .orElseThrow(() -> new DriverApplicationNotFoundException("Application with ID " + id + " not found"));

        String userRole = authentication.getName().split(":")[1];

        if (Objects.equals(userRole, "driver")) {
            String userEmail = authentication.getName().split(":")[0];
            User user = userRepository.findByEmailAndRoleName(userEmail, "driver")
                    .orElseThrow(() -> new UserNotFoundException("User with email " + userEmail + " not found"));

            if (application.getUser() != user) {
                throw new IllegalArgumentException("Application with ID " + id + " doesn't belong to the user");
            }
        }

        DriverApplicationDetailsDTO result = new DriverApplicationDetailsDTO();

        if (Objects.equals(userRole, "system_administrator")) {
            result.setReviewerID(application.getReviewedBy() != null ? application.getReviewedBy().getId() : null);
        }

        result.setId(application.getId());
        result.setUserEmail(application.getUser().getEmail());
        result.setStatus(application.getStatus());
        result.setSubmittedAt(application.getSubmittedAt());
        result.setReviewedAt(application.getReviewedAt());
        result.setRejectionReason(application.getRejectionReason());

        List<CreateDriverDocumentResponseDTO> documents = new ArrayList<>();
        for (DriverDocument document: application.getDriverDocuments()) {
            CreateDriverDocumentResponseDTO shorterDocument = new CreateDriverDocumentResponseDTO(
                    document.getDocumentType(),
                    document.getDocumentPhotoUrl(),
                    document.getExpiryDate()
            );
            documents.add(shorterDocument);
        }

        result.setDriverDocuments(documents);

        return result;
    }

    @Transactional(readOnly = true)
    public List<DriverApplicationSummaryDTO> listUserApplications(
            int page,
            int size,
            Authentication authentication) {

        String userEmail = authentication.getName().split(":")[0];

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<DriverApplicationRequest> userApplications = driverApplicationRequestRepository.findByUserEmail(userEmail, pageable);
        return userApplications.getContent().stream()
                .map(driverApplicationReadMapper::map)
                .toList();
    }

    public void changeStatus(
            Integer id,
            DriverApplicationStatus status,
            String rejectionReason,
            Authentication authentication) {

        String userEmail = authentication.getName().split(":")[0];
        User user = userRepository.findByEmailAndRoleName(userEmail, "system_administrator")
                .orElseThrow(() -> new UserNotFoundException("User with email " + userEmail + " not found"));

        DriverApplicationRequest application = driverApplicationRequestRepository.findById(id)
                .orElseThrow(() -> new DriverApplicationNotFoundException("Application with ID " + id + " not found"));

        if (status == DriverApplicationStatus.PENDING) {
            throw new IllegalArgumentException("Application status can't be set back to 'PENDING'");
        }

        if (application.getStatus() == DriverApplicationStatus.APPROVED
                || application.getStatus() == DriverApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Application is already resolved and can't be modified");
        }

        if (status == DriverApplicationStatus.REJECTED) {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new IllegalArgumentException("Rejection reason must be provided when rejecting an application");
            }
            application.setRejectionReason(rejectionReason);
        } else if (rejectionReason != null && !rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason should only be set when the application is rejected");
        }

        application.setStatus(status);

        if (status == DriverApplicationStatus.APPROVED || status == DriverApplicationStatus.REJECTED) {
            application.setReviewedAt(LocalDateTime.now());
            application.setReviewedBy(user);
        }

        if (status == DriverApplicationStatus.APPROVED) {
            User driver = application.getUser();
            userService.verifyUser(driver);
        }

        User driver = application.getUser();
        webSocketHandler.sendRefreshSignal(driver.getEmail(), "driver/driver-applications");

        driverApplicationRequestRepository.save(application);
    }
}
