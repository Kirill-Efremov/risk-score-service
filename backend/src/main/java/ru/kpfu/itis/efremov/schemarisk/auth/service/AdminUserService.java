package ru.kpfu.itis.efremov.schemarisk.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.efremov.schemarisk.auth.dto.UpdateUserRequest;
import ru.kpfu.itis.efremov.schemarisk.auth.dto.UserResponse;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.LastAdminCannotBeDisabledException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.SelfDeactivationNotAllowedException;
import ru.kpfu.itis.efremov.schemarisk.auth.exception.UserNotFoundException;
import ru.kpfu.itis.efremov.schemarisk.auth.model.UserRole;
import ru.kpfu.itis.efremov.schemarisk.auth.persistence.UserEntity;
import ru.kpfu.itis.efremov.schemarisk.auth.persistence.UserRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AdminUserService(
            UserRepository userRepository,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(UserRole role, Boolean active) {
        return userRepository.findAll().stream()
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> active == null || user.isActive() == active)
                .sorted(Comparator.comparing(UserEntity::getCreatedAt).reversed())
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        UserEntity user = getUser(userId);
        UserEntity currentUser = getCurrentUser();

        UserRole nextRole = request.getRole() != null ? request.getRole() : user.getRole();
        boolean nextActive = request.getActive() != null ? request.getActive() : user.isActive();

        validateSelfUpdate(user, currentUser, nextRole, nextActive);
        validateLastAdminConstraint(user, nextRole, nextActive);

        user.setRole(nextRole);
        user.setActive(nextActive);
        user.setUpdatedAt(Instant.now());

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserResponse deactivateUser(Long userId) {
        UserEntity user = getUser(userId);
        UserEntity currentUser = getCurrentUser();

        if (user.getId().equals(currentUser.getId())) {
            throw new SelfDeactivationNotAllowedException();
        }

        validateLastAdminConstraint(user, user.getRole(), false);

        if (!user.isActive()) {
            return UserResponse.fromEntity(user);
        }

        user.setActive(false);
        user.setUpdatedAt(Instant.now());
        return UserResponse.fromEntity(userRepository.save(user));
    }

    private void validateSelfUpdate(UserEntity targetUser, UserEntity currentUser, UserRole nextRole, boolean nextActive) {
        if (!targetUser.getId().equals(currentUser.getId())) {
            return;
        }
        if (!nextActive) {
            throw new SelfDeactivationNotAllowedException();
        }
    }

    private void validateLastAdminConstraint(UserEntity user, UserRole nextRole, boolean nextActive) {
        boolean adminPrivilegesReduced = user.getRole() == UserRole.ADMIN
                && user.isActive()
                && (!nextActive || nextRole != UserRole.ADMIN);

        if (!adminPrivilegesReduced) {
            return;
        }

        long activeAdmins = userRepository.countByRoleAndActive(UserRole.ADMIN, true);
        if (activeAdmins <= 1) {
            throw new LastAdminCannotBeDisabledException();
        }
    }

    private UserEntity getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserEntity getCurrentUser() {
        String username = currentUserService.currentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}
