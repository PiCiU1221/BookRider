package edu.zut.bookrider.unit.service;

import edu.zut.bookrider.dto.ChangePasswordDto;
import edu.zut.bookrider.exception.InvalidPasswordException;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.Role;
import edu.zut.bookrider.model.User;
import edu.zut.bookrider.repository.UserRepository;
import edu.zut.bookrider.security.SecurityUtils;
import edu.zut.bookrider.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
    public void whenOldPasswordIsCorrect_thenChangePasswordSuccessfully() {
        Library library = new Library();
        library.setId(1);

        User librarian = new User();
        librarian.setId("Random");
        librarian.setUsername("librarian");
        librarian.setPassword("password");
        librarian.setLibrary(library);

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setOldPassword("oldPassword");
        changePasswordDto.setNewPassword("newPassword");

        UserRepository mockUserRepository = mock(UserRepository.class);
        PasswordEncoder mockPasswordEncoder = mock(PasswordEncoder.class);

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn("librarian:1");

        SecurityContextHolder.setContext(new SecurityContextImpl());
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);

        try (MockedStatic<SecurityUtils> mock = Mockito.mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getFirstAuthority).thenReturn("ROLE_librarian");

            UserService userService = new UserService(mockUserRepository, mockPasswordEncoder);

            when(mockUserRepository.findByUsernameAndLibraryId("librarian", 1))
                    .thenReturn(Optional.of(librarian));
            when(mockPasswordEncoder.matches("oldPassword", "password"))
                    .thenReturn(true);
            when(mockPasswordEncoder.encode("newPassword"))
                    .thenReturn("encodedNewPassword");

            userService.changePassword(changePasswordDto);

            assertEquals("encodedNewPassword", librarian.getPassword());
            verify(mockUserRepository, times(1)).save(librarian);
            verify(mockPasswordEncoder, times(1)).encode("newPassword");
        }
    }

    @Test
    public void whenOldPasswordIsIncorrect_thenThrowInvalidPasswordException() {
        User librarian = new User();
        librarian.setId("Random");
        librarian.setUsername("librarian");
        librarian.setPassword("password");

        Library library = new Library();
        library.setId(1);
        librarian.setLibrary(library);

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setOldPassword("wrongPassword");
        changePasswordDto.setNewPassword("newPassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("librarian:1");

        SecurityContextHolder.setContext(new SecurityContextImpl());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try (MockedStatic<SecurityUtils> mock = Mockito.mockStatic(SecurityUtils.class)) {
            mock.when(SecurityUtils::getFirstAuthority).thenReturn("ROLE_USER");

            when(userRepository.findByUsernameAndLibraryId("librarian", 1)).thenReturn(Optional.of(librarian));
            when(passwordEncoder.matches("wrongPassword", "password")).thenReturn(false);

            InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
                userService.changePassword(changePasswordDto);
            });

            assertEquals("Old password is not correct", exception.getMessage());

            verify(userRepository, never()).save(any(User.class));
        }
    }
}
