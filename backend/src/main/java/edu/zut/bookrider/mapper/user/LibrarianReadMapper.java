package edu.zut.bookrider.mapper.user;

import edu.zut.bookrider.dto.CreateLibrarianResponseDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.User;
import org.springframework.stereotype.Component;

@Component
public class LibrarianReadMapper implements Mapper<User, CreateLibrarianResponseDTO> {

    @Override
    public CreateLibrarianResponseDTO map(User user) {
        return new CreateLibrarianResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword()
        );
    }
}
