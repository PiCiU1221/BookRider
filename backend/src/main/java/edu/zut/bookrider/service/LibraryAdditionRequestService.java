package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.dto.CreateLibraryAdditionDTO;
import edu.zut.bookrider.dto.CreateLibraryAdditionResponseDTO;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.LibraryAdditionRequest;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.model.enums.LibraryAdditionStatus;
import edu.zut.bookrider.repository.LibraryAdditionRequestRepository;
import edu.zut.bookrider.repository.LibraryRepository;
import edu.zut.bookrider.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LibraryAdditionRequestService {

    private final UserRepository userRepository;
    private final LibraryAdditionRequestRepository libraryAdditionRequestRepository;
    private final LibraryRepository libraryRepository;

    private final AddressService addressService;

    @Transactional
    public CreateLibraryAdditionResponseDTO createLibraryRequest(
            @Valid CreateLibraryAdditionDTO createLibraryAdditionDTO,
            Authentication authentication) {

        String libraryAdminEmail = authentication.getName().split(":")[0];

        User user = userRepository.findByEmailAndRoleName(libraryAdminEmail, "library_administrator")
                .orElseThrow(() -> new IllegalArgumentException("User with the provided email and 'library_administrator' role doesn't exist"));

        if (libraryRepository.existsByAddress(createLibraryAdditionDTO.getStreet(),
                createLibraryAdditionDTO.getCity(),
                createLibraryAdditionDTO.getPostalCode())) {
            throw new IllegalArgumentException("A library with the same address already exists.");
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

        Address address = addressService.createAddress(createAddressDTO);

        libraryRequest.setAddress(address);

        libraryRequest.setLibraryName(createLibraryAdditionDTO.getLibraryName());
        libraryRequest.setPhoneNumber(createLibraryAdditionDTO.getPhoneNumber());
        libraryRequest.setEmail(createLibraryAdditionDTO.getLibraryEmail());
        libraryRequest.setStatus(LibraryAdditionStatus.PENDING);

        LibraryAdditionRequest savedLibraryRequest = libraryAdditionRequestRepository.save(libraryRequest);

        return new CreateLibraryAdditionResponseDTO(
                savedLibraryRequest.getId(),
                user.getEmail(),
                savedLibraryRequest.getAddress(),
                savedLibraryRequest.getLibraryName(),
                savedLibraryRequest.getPhoneNumber(),
                savedLibraryRequest.getEmail(),
                savedLibraryRequest.getStatus().toString()
        );
    }
}
