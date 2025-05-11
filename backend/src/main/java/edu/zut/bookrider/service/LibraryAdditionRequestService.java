package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.*;
import edu.zut.bookrider.exception.LibraryRequestNotFoundException;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.mapper.libraryAddition.LibraryAdditionReadMapper;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.LibraryAdditionRequest;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import edu.zut.bookrider.repository.LibraryAdditionRequestRepository;
import edu.zut.bookrider.repository.LibraryRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class LibraryAdditionRequestService {

    private final UserRepository userRepository;
    private final LibraryAdditionRequestRepository libraryAdditionRequestRepository;
    private final LibraryRepository libraryRepository;
    private final AddressService addressService;
    private final LibraryAdditionReadMapper libraryAdditionReadMapper;
    private final LibraryService libraryService;
    private final UserService userService;

    private final WebSocketHandler webSocketHandler;

    @Transactional
    public CreateLibraryAdditionResponseDTO createLibraryRequest(
            @Valid CreateLibraryAdditionDTO createLibraryAdditionDTO,
            Authentication authentication) {

        String libraryAdminEmail = authentication.getName().split(":")[0];

        User user = userRepository.findByEmailAndRoleName(libraryAdminEmail, "library_administrator")
                .orElseThrow(() -> new IllegalArgumentException("User with the provided email and 'library_administrator' role doesn't exist"));

        if (libraryAdditionRequestRepository.existsByCreatedByAndPendingStatus(user)) {
            throw new IllegalArgumentException("User already has a pending library addition request.");
        }

        if (libraryRepository.existsByAddress(createLibraryAdditionDTO.getStreet(),
                createLibraryAdditionDTO.getCity(),
                createLibraryAdditionDTO.getPostalCode())) {
            throw new IllegalArgumentException("A library with the same address already exists.");
        }

        if (libraryRepository.existsByName(createLibraryAdditionDTO.getLibraryName())) {
            throw new IllegalArgumentException("A library with this name already exists");
        }

        if (libraryRepository.existsByNameAndCity(createLibraryAdditionDTO.getLibraryName(), createLibraryAdditionDTO.getCity())) {
            throw new IllegalArgumentException("A library with the same name already exists in this city.");
        }

        LibraryAdditionRequest libraryRequest = new LibraryAdditionRequest();

        libraryRequest.setCreatedBy(user);

        CreateAddressDTO createAddressDTO = new CreateAddressDTO(
            createLibraryAdditionDTO.getStreet(),
                createLibraryAdditionDTO.getCity(),
                createLibraryAdditionDTO.getPostalCode()
        );

        Address address = addressService.findOrCreateAddress(createAddressDTO);

        libraryRequest.setAddress(address);

        libraryRequest.setLibraryName(createLibraryAdditionDTO.getLibraryName());
        libraryRequest.setPhoneNumber(createLibraryAdditionDTO.getPhoneNumber());
        libraryRequest.setEmail(createLibraryAdditionDTO.getLibraryEmail());
        libraryRequest.setStatus(LibraryAdditionStatus.PENDING);

        LibraryAdditionRequest savedLibraryRequest = libraryAdditionRequestRepository.save(libraryRequest);

        List<User> administrators = userService.getAllAdministrators();
        for (User administrator : administrators) {
            webSocketHandler.sendRefreshSignal(administrator.getEmail(), "administrator/library-requests");
        }

        return new CreateLibraryAdditionResponseDTO(
                savedLibraryRequest.getId(),
                user.getEmail(),
                savedLibraryRequest.getAddress(),
                savedLibraryRequest.getLibraryName(),
                savedLibraryRequest.getPhoneNumber(),
                savedLibraryRequest.getEmail(),
                savedLibraryRequest.getStatus()
        );
    }

    public List<LibraryAdditionSummaryDTO> listRequests(
            List<LibraryAdditionStatus> statuses,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<LibraryAdditionRequest> applicationsPage;

        if (statuses == null || statuses.isEmpty()) {
            applicationsPage = libraryAdditionRequestRepository.findAll(pageable);
        } else {
            applicationsPage = libraryAdditionRequestRepository.findByStatusIn(statuses, pageable);
        }

        return applicationsPage.getContent().stream()
                .map(libraryAdditionReadMapper::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public LibraryRequestDetailsDTO getRequestDetails(
            Integer id,
            Authentication authentication) {

        LibraryAdditionRequest request = libraryAdditionRequestRepository.findById(id)
                .orElseThrow(() -> new LibraryRequestNotFoundException("Request with ID " + id + " not found"));

        String userRole = authentication.getName().split(":")[1];

        if (Objects.equals(userRole, "library_administrator")) {
            String userEmail = authentication.getName().split(":")[0];
            User user = userRepository.findByEmailAndRoleName(userEmail, "library_administrator")
                    .orElseThrow(() -> new UserNotFoundException("User with email " + userEmail + " not found"));

            if (request.getCreatedBy() != user) {
                throw new IllegalArgumentException("Request with ID " + id + " doesn't belong to the user");
            }
        }

        LibraryRequestDetailsDTO result = new LibraryRequestDetailsDTO();

        if (Objects.equals(userRole, "system_administrator")) {
            result.setReviewerId(request.getReviewedBy() != null ? request.getReviewedBy().getId() : null);
        }

        result.setId(request.getId());
        result.setCreatorEmail(request.getCreatedBy().getEmail());
        result.setAddress(request.getAddress());
        result.setLibraryName(request.getLibraryName());
        result.setPhoneNumber(request.getPhoneNumber());
        result.setLibraryEmail(request.getEmail());
        result.setStatus(request.getStatus());
        result.setSubmittedAt(request.getSubmittedAt());
        result.setReviewedAt(request.getReviewedAt());
        result.setRejectionReason(request.getRejectReason());

        return result;
    }

    @Transactional(readOnly = true)
    public List<LibraryAdditionSummaryDTO> listUserRequests(
            int page,
            int size,
            Authentication authentication) {

        String userEmail = authentication.getName().split(":")[0];

        Pageable pageable = PageRequest.of(page, size);
        Page<LibraryAdditionRequest> userApplications = libraryAdditionRequestRepository.findByUser_Email(userEmail, pageable);
        return userApplications.getContent().stream()
                .map(libraryAdditionReadMapper::map)
                .toList();
    }

    @Transactional
    public void changeStatus(
            Integer id,
            LibraryAdditionStatus status,
            String rejectionReason,
            Authentication authentication) {

        String userEmail = authentication.getName().split(":")[0];
        User user = userRepository.findByEmailAndRoleName(userEmail, "system_administrator")
                .orElseThrow(() -> new UserNotFoundException("User with email " + userEmail + " not found"));

        LibraryAdditionRequest request = libraryAdditionRequestRepository.findById(id)
                .orElseThrow(() -> new LibraryRequestNotFoundException("Request with ID " + id + " not found"));

        if (status == LibraryAdditionStatus.PENDING) {
            throw new IllegalArgumentException("Request status can't be set back to 'PENDING'");
        }

        if (request.getStatus() == LibraryAdditionStatus.APPROVED
                || request.getStatus() == LibraryAdditionStatus.REJECTED) {
            throw new IllegalArgumentException("Request is already resolved and can't be modified");
        }

        if (status == LibraryAdditionStatus.REJECTED) {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new IllegalArgumentException("Rejection reason must be provided when rejecting a request");
            }
            request.setRejectReason(rejectionReason);
        } else if (rejectionReason != null && !rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason should only be set when the request is rejected");
        }

        request.setStatus(status);

        if (status == LibraryAdditionStatus.APPROVED || status == LibraryAdditionStatus.REJECTED) {
            request.setReviewedAt(LocalDateTime.now());
            request.setReviewedBy(user);
        }

        if (status == LibraryAdditionStatus.APPROVED) {
            Library createdLibrary = libraryService.createLibrary(request);
            User requestCreator = request.getCreatedBy();

            userService.updateLibrary(requestCreator, createdLibrary);
            userService.verifyUser(requestCreator);
        }

        webSocketHandler.sendRefreshSignal(user.getEmail(), "user/library-requests");

        libraryAdditionRequestRepository.save(request);
    }
}
