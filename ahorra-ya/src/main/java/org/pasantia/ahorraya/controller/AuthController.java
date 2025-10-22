package org.pasantia.ahorraya.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pasantia.ahorraya.dto.auth.AdminRegisterRequest;
import org.pasantia.ahorraya.dto.auth.AuthResponse;
import org.pasantia.ahorraya.dto.auth.LoginRequest;
import org.pasantia.ahorraya.dto.auth.RegisterRequest;
import org.pasantia.ahorraya.model.Admin;
import org.pasantia.ahorraya.model.User;
import org.pasantia.ahorraya.model.enums.UserStatus;
import org.pasantia.ahorraya.repository.AdminRepository;
import org.pasantia.ahorraya.security.JwtService;
import org.pasantia.ahorraya.service.MicroSavingConfigService;
import org.pasantia.ahorraya.service.UserService;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateEmailException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateIdentificationException;
import org.pasantia.ahorraya.validation.uservalidations.DuplicateUsernameException;
import org.pasantia.ahorraya.validation.uservalidations.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller that handles authentication-related endpoints.
 *
 * <p>Exposes endpoints for user registration, admin registration, login and token validation.
 * This controller delegates user and admin persistence and JWT generation to injected services.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Auth management endpoints")
public class AuthController {

    /**
     * Authentication manager used to authenticate credentials.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Password encoder used to hash plain-text passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Service that issues and validates JWT tokens.
     */
    private final JwtService jwtService;

    /**
     * Service for user-related operations (lookup, creation).
     */
    private final UserService userService;

    /**
     * Repository for admin persistence.
     */
    private final AdminRepository adminRepository;

    /**
     * Service to create default micro-saving configurations for new users.
     */
    private final MicroSavingConfigService microSavingConfigService;

    /**
     * Register a new user, create a default micro-saving configuration and return a JWT.
     *
     * @param request the registration request containing user details (validated)
     * @return ResponseEntity with AuthResponse containing JWT and user info (HTTP 201)
     * @throws DuplicateUsernameException if username already exists
     * @throws DuplicateEmailException if email already exists
     * @throws DuplicateIdentificationException if identification number already exists
     */
    @Operation(summary = "Register a new user", description = "Registers a new user with a saving account and default micro-saving configuration.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("Attempting user registration: {}", request.getUsername());

        if (userService.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateUsernameException(request.getUsername());
        }

        if (userService.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException(request.getEmail());
        }

        if (userService.findByIdentificationNumber(request.getIdentificationNumber()).isPresent()) {
            throw new DuplicateIdentificationException(request.getIdentificationNumber());
        }


        User user = User.builder()
                .identificationNumber(request.getIdentificationNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userService.createUser(user);
        log.info("User registered successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        try {
            microSavingConfigService.createDefaultConfig(savedUser.getId());
            log.info("MicroSavingConfig created for user: {}", savedUser.getUsername());
        } catch (Exception e) {
            log.warn("Failed to create MicroSavingConfig for user {}: {}", savedUser.getUsername(), e.getMessage());
        }

        String token = jwtService.generateToken(savedUser);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role("USER")
                .expiresIn(86400000L)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate a user or admin and return a JWT token with basic user info.
     *
     * @param request the login request containing username and password (validated)
     * @return ResponseEntity with AuthResponse containing JWT and user/admin info (HTTP 200)
     * @throws UserNotFoundException if an authenticated principal cannot be mapped to a stored user or admin
     */
    @Operation(summary = "User login", description = "Authenticates a user or admin and returns a JWT token.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Attempting login: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User user = userService.findByUsername(username).orElse(null);
        Admin admin = user == null ? adminRepository.findByEmail(username).orElse(null) : null;

        if (user == null && admin == null) {
            log.error("Authenticated principal not found in DB: {}", username);
            throw new UserNotFoundException("User not found after authentication");
        }

        String token = jwtService.generateToken(userDetails);
        String role;
        String email;
        String displayUsername;

        if (user != null) {
            role = "USER";
            email = user.getEmail();
            displayUsername = user.getUsername();
            log.info("Successful login - User: {} ({})", user.getUsername(), user.getEmail());
        } else {
            role = "ADMIN";
            email = admin.getEmail();
            displayUsername = admin.getEmail();
            log.info("Successful login - Admin: {}", admin.getEmail());
        }

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(displayUsername)
                .email(email)
                .role(role)
                .expiresIn(86400000L)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Register a new admin and return a JWT.
     *
     * @param request the admin registration request (validated)
     * @return ResponseEntity with AuthResponse containing JWT and admin info (HTTP 201)
     * @throws DuplicateEmailException if admin email already exists
     */
    @Operation(summary = "Register a new admin", description = "Registers a new admin user.")
    @PostMapping("/register-admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        log.info("Attempting admin registration: {}", request.getEmail());

        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Admin admin = Admin.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .build();

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Admin registered successfully: {}", savedAdmin.getEmail());

        String token = jwtService.generateToken(savedAdmin);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(savedAdmin.getEmail())
                .email(savedAdmin.getEmail())
                .role("ADMIN")
                .expiresIn(86400000L)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Validate a JWT token and return the associated username if valid.
     *
     * @param token the Authorization header value containing the Bearer token
     * @return ResponseEntity with a map containing validation result and username
     * @throws IllegalArgumentException if the token is invalid or expired
     */
    @Operation(summary = "Validate JWT token", description = "Validates a JWT token and returns the associated username if valid.")
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String token) {

        try {
            String jwt = token.substring(7);
            String username = jwtService.extractUsername(jwt);
            return ResponseEntity.ok(Map.of("valid", true, "username", username));
        } catch (Exception e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired token");
        }
    }
}

