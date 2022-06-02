package pt.ua.deti.tqs.sendasnack.core.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.AccountRoleEnum;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.AlreadyExistentUserException;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.UserNotFoundException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.User;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.UserRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.services.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910", AccountRoleEnum.RIDER);
    }

    @Test
    @DisplayName("Find user by username.")
    void findByUsername() {

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        assertThat(userService.findByUsername(user.getUsername()))
                .isNotNull()
                .isEqualTo(user);

        verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        verify(userRepository, Mockito.times(1)).existsByUsername(Mockito.anyString());

    }

    @Test
    @DisplayName("Find user by non-existent username.")
    void findByNonExistentUsername() {

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);

        assertThatThrownBy(() -> userService.findByUsername(user.getUsername()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("The user %s could not be found.", user.getUsername()));

        verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        verify(userRepository, Mockito.times(1)).existsByUsername(Mockito.anyString());

    }

    @Test
    @DisplayName("Find user by email.")
    void findByEmail() {

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThat(userService.findByEmail(user.getEmail()))
                .isNotNull()
                .isEqualTo(user);

        verify(userRepository, Mockito.times(1)).findByEmail(Mockito.anyString());
        verify(userRepository, Mockito.times(1)).existsByEmail(Mockito.anyString());

    }

    @Test
    @DisplayName("Find user by non-existent email.")
    void findByNonExistentEmail() {

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> userService.findByEmail(user.getEmail()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("The user %s could not be found.", user.getEmail());

        verify(userRepository, Mockito.times(0)).findByEmail(Mockito.anyString());
        verify(userRepository, Mockito.times(1)).existsByEmail(Mockito.anyString());

    }

    @Test
    @DisplayName("Register already existent user.")
    void registerExistentUser() {

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        assertThrows(AlreadyExistentUserException.class, () -> userService.registerUser(user));

    }

}