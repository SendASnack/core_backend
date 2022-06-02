package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pt.ua.deti.tqs.sendasnack.core.backend.model.User;

import java.util.Collections;

@Service
public class SpringUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public SpringUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userService.findByUsername(username);
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), Collections.singleton(user.getAccountRoles()));
    }

}