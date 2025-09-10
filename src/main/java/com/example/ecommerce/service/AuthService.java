package com.example.ecommerce.service;

import com.example.ecommerce.util.builder.ResponseBuilder;
import com.example.ecommerce.util.builder.UserBuilder;
import com.example.ecommerce.util.checker.AuthChecker;
import com.example.ecommerce.util.checker.UserChecker;
import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.RegisterRequest;
import com.example.ecommerce.dto.response.JwtAuthenticationResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthChecker authChecker;
    private final UserChecker userChecker;

    public JwtAuthenticationResponse register(RegisterRequest request) {

        authChecker.checkUsernameAndEmailNotTaken(request.getUsername(), request.getEmail());
        User user = UserBuilder.fromRegisterRequest(request, passwordEncoder);
        user = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseBuilder.createJwtResponse(jwt, user);
    }

    public JwtAuthenticationResponse login(LoginRequest request) {
        User user = userChecker.checkUserExistsByUsername(request.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseBuilder.createJwtResponse(jwt, user);
    }
}