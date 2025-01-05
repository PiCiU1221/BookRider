package edu.zut.bookrider.mapper.user;

import edu.zut.bookrider.dto.LibrarianDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.User;
import org.springframework.stereotype.Component;

@Component
public class LibrarianReadMapper implements Mapper<User, LibrarianDTO> {

    @Override
    public LibrarianDTO map(User user) {
        return new LibrarianDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
