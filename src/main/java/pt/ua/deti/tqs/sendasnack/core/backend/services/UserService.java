package pt.ua.deti.tqs.sendasnack.core.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.AlreadyExistentUserException;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.UserNotFoundException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.User;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    public User findByUsername(String username) {

        if (!userRepository.existsByUsername(username))
            throw new UserNotFoundException(String.format("The user %s could not be found.", username));
        return userRepository.findByUsername(username);

    }

    @NonNull
    public User findByEmail(String email) {

        if (!userRepository.existsByEmail(email))
            throw new UserNotFoundException(String.format("The user %s could not be found.", email));
        return userRepository.findByEmail(email);

    }

    public void registerUser(User user) {

        if (userRepository.existsByUsername(user.getUsername()))
            throw new AlreadyExistentUserException("The provided username is already taken.");
        userRepository.save(user);

    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void removeAll() {
        userRepository.deleteAll();
    }

}
