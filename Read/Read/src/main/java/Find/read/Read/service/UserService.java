package Find.read.Read.service;

import Find.read.Read.models.User;
import Find.read.Read.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public boolean checkPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }



    public void saveUser(User user) {
        userRepository.save(user);  // Save user to the database
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);  // Encrypt password before saving
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);  // Validate password
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);  // This queries the database
    }



}
