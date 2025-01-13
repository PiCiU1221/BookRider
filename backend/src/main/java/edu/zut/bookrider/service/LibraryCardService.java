package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.LibraryCardDTO;
import edu.zut.bookrider.exception.InvalidLibraryCardException;
import edu.zut.bookrider.exception.LibraryCardNotFoundException;
import edu.zut.bookrider.exception.UserNotFoundException;
import edu.zut.bookrider.mapper.libraryCard.LibraryCardMapper;
import edu.zut.bookrider.model.LibraryCard;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.LibraryCardRepository;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LibraryCardService {

    private final UserRepository userRepository;
    private final LibraryCardRepository libraryCardRepository;
    private final LibraryCardMapper libraryCardMapper;

    @Transactional
    public LibraryCardDTO addLibraryCard(@Valid LibraryCardDTO input) {
        User user = userRepository.findById(input.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User with the id '" + input.getUserId() + "' not found"));

        if (libraryCardRepository.existsByCardId(input.getCardId())) {
            throw new IllegalStateException("Library card with the provided ID already in use");
        }

        LibraryCard libraryCard = libraryCardMapper.toEntity(input, user);
        LibraryCard savedCard = libraryCardRepository.save(libraryCard);

        return libraryCardMapper.toDto(savedCard);
    }

    @Transactional
    public List<LibraryCardDTO> getUsersLibraryCards(
            String userId,
            int page,
            int size,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<LibraryCard> libraryCardsPage;

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with the id '" + userId + "' not found");
        }

        if (Objects.equals(SecurityUtils.getFirstAuthority(), "ROLE_user")) {
            String userEmail = authentication.getName().split(":")[0];

            User user = userRepository.findByEmailAndRoleName(userEmail, "user")
                    .orElseThrow(() -> new UserNotFoundException("User with the email '" + userEmail + "' not found"));

            if (!Objects.equals(user.getId(), userId)) {
                throw new IllegalArgumentException("Requester ID is not the same as the userId");
            }
        }

        libraryCardsPage = libraryCardRepository.findByUserId(userId, pageable);

        return libraryCardsPage.getContent().stream()
                .map(libraryCardMapper::toDto)
                .toList();
    }

    public void deleteLibraryCard(Integer libraryCardId) {
        LibraryCard libraryCard = libraryCardRepository.findById(libraryCardId)
                .orElseThrow(() -> new LibraryCardNotFoundException("Library card with ID:'" + libraryCardId + "' not found"));

        libraryCardRepository.delete(libraryCard);
    }

    public void validateLibraryCard(String userId) {
        List<LibraryCard> libraryCards = libraryCardRepository.findByUserId(userId, Pageable.unpaged())
                .getContent();

        LibraryCard validCard = libraryCards.stream()
                .filter(card -> card.getExpirationDate().isAfter(LocalDate.now()))
                .findFirst()
                .orElse(null);

        if (validCard == null) {
            throw new InvalidLibraryCardException("The user's library card is missing or has expired");
        }
    }

}
