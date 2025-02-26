package edu.zut.bookrider.mapper.libraryCard;

import edu.zut.bookrider.dto.LibraryCardDTO;
import edu.zut.bookrider.model.LibraryCard;
import edu.zut.bookrider.model.User;
import org.springframework.stereotype.Component;

@Component
public class LibraryCardMapper {

    public LibraryCard toEntity(LibraryCardDTO dto, User user) {
        return LibraryCard.builder()
                .user(user)
                .cardId(dto.getCardId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .expirationDate(dto.getExpirationDate())
                .build();
    }

    public LibraryCardDTO toDto(LibraryCard libraryCard) {
        return LibraryCardDTO.builder()
                .userId(libraryCard.getUser().getId())
                .cardId(libraryCard.getCardId())
                .firstName(libraryCard.getFirstName())
                .lastName(libraryCard.getLastName())
                .expirationDate(libraryCard.getExpirationDate())
                .build();
    }
}
