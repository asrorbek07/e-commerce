package com.example.ecommerce.util.builder;

import com.example.ecommerce.dto.request.RegisterRequest;
import com.example.ecommerce.model.User;
import com.example.ecommerce.model.vo.Role;
import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.password.PasswordEncoder;

@UtilityClass
public final class UserBuilder {

    public User fromRegisterRequest(RegisterRequest request, PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .build();
    }

}