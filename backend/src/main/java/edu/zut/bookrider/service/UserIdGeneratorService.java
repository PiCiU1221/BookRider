package edu.zut.bookrider.service;

import edu.zut.bookrider.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserIdGeneratorService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ID_LENGTH = 10;
    private final Random random = new SecureRandom();

    private final UserRepository userRepository;

    public String generateUniqueId() {
        String generatedId;
        do {
            generatedId = generateRandomId();
        } while (userRepository.existsById(generatedId));
        return generatedId;
    }

    private String generateRandomId() {
        StringBuilder id = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(index));
        }
        return id.toString();
    }
}
