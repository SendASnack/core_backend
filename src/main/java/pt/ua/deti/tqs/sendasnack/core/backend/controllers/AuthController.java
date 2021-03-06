package pt.ua.deti.tqs.sendasnack.core.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.UserDAO;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.BadRequestException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.RiderUser;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.User;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.LoginRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.MessageResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.LoginResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.JWTTokenUtils;
import pt.ua.deti.tqs.sendasnack.core.backend.services.SpringUserDetailsService;
import pt.ua.deti.tqs.sendasnack.core.backend.services.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final SpringUserDetailsService springUserDetailsService;
    private final JWTTokenUtils jwtTokenUtils;

    @Autowired
    public AuthController(PasswordEncoder passwordEncoder, UserService userService, SpringUserDetailsService springUserDetailsService, JWTTokenUtils jwtTokenUtils) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.springUserDetailsService = springUserDetailsService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @PostMapping("/register")
    public MessageResponse registerUser(@RequestBody UserDAO userDAO) {

        // Encode password
        String userPassword = userDAO.getPassword();
        userDAO.setPassword(passwordEncoder.encode(userPassword));

        userService.registerUser(userDAO.toDataEntity());

        return new MessageResponse("The user was successfully registered!");

    }

    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody LoginRequest loginRequest) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        if (email == null || password == null)
            throw new BadRequestException("Please provide a valid request body.");

        User user = userService.findByEmail(email);
        UserDetails userDetails = springUserDetailsService.loadUserByUsername(user.getUsername());

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new BadCredentialsException("The provided password is wrong.");

        String token = jwtTokenUtils.generateToken(userDetails);

        if (user instanceof RiderUser) {
            ((RiderUser) user).setAvailabilityStatus(null);
            ((RiderUser) user).setRejectedDeliveries(null);
            ((RiderUser) user).setAcceptedDeliveries(null);
        }

        return new LoginResponse("Authentication succeeded.", token, user);

    }

}
