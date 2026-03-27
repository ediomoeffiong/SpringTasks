package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.dto.AuthResponse;
import com.fifthlab.springtasks.dto.LoginRequest;
import com.fifthlab.springtasks.dto.RegisterRequest;
import com.fifthlab.springtasks.model.User;
import com.fifthlab.springtasks.repository.UserRepository;
import com.fifthlab.springtasks.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public void register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles("ROLE_USER");

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        // Find user by either username or email
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getEmailOrUsername());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(loginRequest.getEmailOrUsername());
        }

        User user = userOpt.orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid username or password"));

        // Authenticate using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);

        return new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail());
    }
}
