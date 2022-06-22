package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.User;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SpringUserDetailsService springUserDetailsService;

    private UserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910");
        userDetails = new org.springframework.security.core.userdetails.User("Hugo1307", "123456", Collections.emptyList());
    }

    @Test
    @DisplayName("Load UserDetails instance by username")
    void loadUserByUsername() {

        when(userService.findByUsername(user.getUsername())).thenReturn(user);

        assertThat(springUserDetailsService.loadUserByUsername(user.getUsername()))
                .isNotNull()
                .isEqualTo(userDetails);

        verify(userService, Mockito.times(1)).findByUsername(Mockito.anyString());

    }

}