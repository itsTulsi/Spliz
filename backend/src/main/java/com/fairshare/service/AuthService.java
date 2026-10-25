package com.fairshare.service;

import com.fairshare.dto.*;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.entity.UserEntity;
import com.fairshare.entity.UserSessionEntity;
import com.fairshare.exception.BusinessException;
import com.fairshare.exception.UnauthorizedException;
import com.fairshare.repository.GroupMemberRepository;
import com.fairshare.repository.UserRepository;
import com.fairshare.repository.UserSessionRepository;
import com.fairshare.util.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final UpiPaymentService upiPaymentService;
    private final GroupMemberRepository groupMemberRepository;

    public AuthService(UserRepository userRepository,
                       UserSessionRepository userSessionRepository,
                       UpiPaymentService upiPaymentService,
                       GroupMemberRepository groupMemberRepository) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.upiPaymentService = upiPaymentService;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("Username '" + username + "' is already taken.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email '" + email + "' is already registered.");
        }

        String upiId = request.getUpiId() != null && !request.getUpiId().isBlank() ? request.getUpiId().trim() : null;
        if (upiId != null) {
            upiPaymentService.validateUpiId(upiId);
        }

        String passwordHash = PasswordUtils.hashPassword(request.getPassword());
        UserEntity user = new UserEntity(username, email, passwordHash, request.getDisplayName().trim(), upiId);
        UserEntity savedUser = userRepository.save(user);

        UserSessionEntity session = createSession(savedUser);
        return new AuthResponse(session.getToken(), mapToDto(savedUser));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        UserEntity user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        if (!PasswordUtils.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        UserSessionEntity session = createSession(user);
        return new AuthResponse(session.getToken(), mapToDto(user));
    }

    @Transactional(readOnly = true)
    public UserDto getMe(UserEntity user) {
        if (user == null) {
            throw new UnauthorizedException("Not authenticated.");
        }
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateSettings(UserEntity currentUser, UpdateSettingsRequest request) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required.");
        }

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found."));

        // 1. Password change (Protected)
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            String currentPassword = request.getCurrentPassword();
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new BusinessException("Current password is required to change password.");
            }
            if (!PasswordUtils.verifyPassword(currentPassword, user.getPasswordHash())) {
                throw new BusinessException("Current password is incorrect.");
            }
            if (request.getNewPassword().length() < 6) {
                throw new BusinessException("New password must be at least 6 characters.");
            }
            user.setPasswordHash(PasswordUtils.hashPassword(request.getNewPassword()));
        }

        // 2. Username update
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim().toLowerCase();
            if (!newUsername.equalsIgnoreCase(user.getUsername())) {
                if (!newUsername.matches("^[a-zA-Z0-9_]{3,30}$")) {
                    throw new BusinessException("Username must be between 3 and 30 characters and contain only letters, numbers, and underscores.");
                }
                userRepository.findByUsernameIgnoreCase(newUsername).ifPresent(other -> {
                    if (!other.getId().equals(user.getId())) {
                        throw new BusinessException("Username '" + newUsername + "' is already taken.");
                    }
                });
                user.setUsername(newUsername);
            }
        }

        // 3. Email update
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (!newEmail.contains("@") || !newEmail.contains(".")) {
                    throw new BusinessException("Please provide a valid email address.");
                }
                userRepository.findByEmailIgnoreCase(newEmail).ifPresent(other -> {
                    if (!other.getId().equals(user.getId())) {
                        throw new BusinessException("Email '" + newEmail + "' is already registered to another account.");
                    }
                });
                user.setEmail(newEmail);
            }
        }

        // 4. Display name update
        boolean displayNameChanged = false;
        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            String newDisplayName = request.getDisplayName().trim();
            if (!newDisplayName.equals(user.getDisplayName())) {
                user.setDisplayName(newDisplayName);
                displayNameChanged = true;
            }
        }

        // 5. UPI ID update
        boolean upiChanged = false;
        if (request.getUpiId() != null) {
            String newUpi = request.getUpiId().trim();
            if (!newUpi.isBlank()) {
                upiPaymentService.validateUpiId(newUpi);
                if (!newUpi.equalsIgnoreCase(user.getUpiId())) {
                    user.setUpiId(newUpi);
                    upiChanged = true;
                }
            } else if (user.getUpiId() != null) {
                user.setUpiId(null);
                upiChanged = true;
            }
        }

        UserEntity saved = userRepository.save(user);

        // Update corresponding group members if display name or UPI changed
        if (displayNameChanged || upiChanged) {
            List<GroupMemberEntity> linkedMembers = groupMemberRepository.findByUserId(saved.getId());
            for (GroupMemberEntity member : linkedMembers) {
                if (displayNameChanged) {
                    member.setDisplayName(saved.getDisplayName());
                }
                if (upiChanged) {
                    member.setUpiId(saved.getUpiId());
                }
                groupMemberRepository.save(member);
            }
        }

        return mapToDto(saved);
    }

    @Transactional
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            userSessionRepository.deleteByToken(token);
        }
    }

    private UserSessionEntity createSession(UserEntity user) {
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
        UserSessionEntity session = new UserSessionEntity(token, user, expiresAt);
        return userSessionRepository.save(session);
    }

    public UserDto mapToDto(UserEntity u) {
        return new UserDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getDisplayName(),
                u.getUpiId(),
                u.getCreatedAt()
        );
    }
}
