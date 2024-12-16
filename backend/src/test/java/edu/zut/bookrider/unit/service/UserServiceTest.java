package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void whenCorrectInput_thenDoNotThrowException() {
        Role driverRole = new Role();
        driverRole.setId(1);
        driverRole.setName("driver");

        User user = new User();
        user.setId("RANDOM");
        user.setRole(driverRole);

        Address address = new Address();
        address.setId(1);
        address.setStreet("something");
        address.setCity("something");
        address.setPostalCode("something");
        address.setLatitude(BigDecimal.valueOf(0.0));
        address.setLongitude(BigDecimal.valueOf(0.0));

        Library library = new Library();
        library.setId(1);
        library.setAddress(address);
        library.setName("something");
        library.setPhoneNumber("something");

        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateLibrary(user, library);

        assertEquals(library, user.getLibrary());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testVerifyUser() {
        Role driverRole = new Role();
        driverRole.setId(1);
        driverRole.setName("driver");

        User user = new User();
        user.setId("RANDOM");
        user.setRole(driverRole);

        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.verifyUser(user);

        assertTrue(user.getIsVerified());
        verify(userRepository, times(1)).save(user);
    }
}
