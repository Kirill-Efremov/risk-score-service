package ru.kpfu.itis.efremov.schemarisk.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.efremov.schemarisk.auth.dto.AuthResponse;
import ru.kpfu.itis.efremov.schemarisk.auth.dto.CurrentUserResponse;
import ru.kpfu.itis.efremov.schemarisk.auth.dto.UserResponse;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.AuthenticationRequiredException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.InvalidCredentialsException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.UserAlreadyExistsException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.UserDisabledException;
import ru.kpfu.itis.efremov.schemarisk.auth.model.UserRole;
import ru.kpfu.itis.efremov.schemarisk.auth.persistence.UserEntity;
import ru.kpfu.itis.efremov.schemarisk.auth.persistence.UserRepository;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public UserResponse register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        userRepository.lockTable();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new UserAlreadyExistsException(normalizedUsername);
        }

        Instant now = Instant.now();
        UserEntity user = new UserEntity();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(userRepository.count() == 0 ? UserRole.ADMIN : UserRole.USER);
        user.setActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        try {
            return UserResponse.fromEntity(userRepository.save(user));
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException(normalizedUsername);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        UserEntity user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new UserDisabledException(normalizedUsername);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                CurrentUserResponse.fromEntity(user)
        );
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser() {
        String username = currentUserService.currentUsernameOptional()
                .orElseThrow(AuthenticationRequiredException::new);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(AuthenticationRequiredException::new);

        if (!user.isActive()) {
            throw new UserDisabledException(username);
        }

        return CurrentUserResponse.fromEntity(user);
    }

    private String normalizeUsername(String username) {
        return username.trim();
    }
}
