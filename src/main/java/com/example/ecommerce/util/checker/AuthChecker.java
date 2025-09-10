package com.example.ecommerce.util.checker;

import com.example.ecommerce.exception.ResourceAlreadyExistsException;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthChecker {

    private final UserRepository userRepository;

    public void checkUsernameAndEmailNotTaken(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("User", "username", username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User", "email", email);
        }
    }

}