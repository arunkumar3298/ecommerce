package com.arun.ecommerce.user;

import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.user.dto.ChangePasswordRequest;
import com.arun.ecommerce.user.dto.UpdateProfileRequest;
import com.arun.ecommerce.user.dto.UserProfileResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.arun.ecommerce.auth.UserRepository;
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserProfileResponse getProfile(User user) {
        return toResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(User user,
                                             UpdateProfileRequest request) {
        user.setName(request.getName());
        return toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(User user,
                               ChangePasswordRequest request) {

        // Verify current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(),
                user.getPassword())) {
            throw new IllegalArgumentException(
                    "Current password is incorrect");
        }

        // Prevent using same password again
        if (passwordEncoder.matches(request.getNewPassword(),
                user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── Private Mapper ────────────────────────────────────────

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isVerified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
