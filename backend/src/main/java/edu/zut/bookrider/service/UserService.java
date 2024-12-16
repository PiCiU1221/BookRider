package edu.zut.bookrider.service;

import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public void updateLibrary(User user, Library library) {

        user.setLibrary(library);
        userRepository.save(user);
    }

    public void verifyUser(User user) {

        user.setIsVerified(true);
        userRepository.save(user);
    }
}
