package pt.ua.deti.tqs.sendasnack.core.backend.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.JWTTokenUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class JWTTokenUtilsTest {

    private UserDetails userDetails;

    private JWTTokenUtils jwtTokenUtils;

    @BeforeEach
    void setUp() {
        userDetails = new User("Hugo1307", "123456", Collections.emptyList());
        jwtTokenUtils = new JWTTokenUtils("SECRET");
    }

    @Test
    void getUsernameFromToken() {
        String token = jwtTokenUtils.generateToken(userDetails);
        assertThat(jwtTokenUtils.getUsernameFromToken(token)).isEqualTo(userDetails.getUsername());
    }

    @Test
    void getExpirationDateFromToken() {

        String token = jwtTokenUtils.generateToken(userDetails);
        Date expectedExpirationDate = new Date(System.currentTimeMillis() + JWTTokenUtils.JWT_TOKEN_VALIDITY * 1000);

        assertThat(jwtTokenUtils.getExpirationDateFromToken(token)).isInSameSecondAs(expectedExpirationDate);

    }

    @Test
    void getClaimFromToken() {
        String token = jwtTokenUtils.generateToken(userDetails);
        assertThat(jwtTokenUtils.getClaimFromToken(token, Claims::getSubject)).isEqualTo(userDetails.getUsername());
    }

    @Test
    void validateToken() {

        String token = jwtTokenUtils.generateToken(userDetails);
        assertThat(jwtTokenUtils.validateToken(token, userDetails)).isTrue();

    }

}