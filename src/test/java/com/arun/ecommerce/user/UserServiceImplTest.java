package com.arun.ecommerce.user;

import com.arun.ecommerce.auth.UserRepository;
import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.entity.enums.Role;
import com.arun.ecommerce.user.dto.ChangePasswordRequest;
import com.arun.ecommerce.user.dto.UpdateProfileRequest;
import com.arun.ecommerce.user.dto.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository  userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User                 user;
    private UpdateProfileRequest updateRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Arun Kumar")
                .email("arun@test.com")
                .password("encoded_old_password")
                .role(Role.USER)
                .isVerified(true)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        updateRequest = new UpdateProfileRequest();
        updateRequest.setName("Arun K");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword456");
    }

    // ── getProfile() ──────────────────────────────────────────────

    @Test
    @DisplayName("getProfile: should return correct user details")
    void getProfile_shouldReturnUserDetails() {
        UserProfileResponse response = userService.getProfile(user);

        assertNotNull(response);
        assertEquals(1L,             response.getId());
        assertEquals("Arun Kumar",   response.getName());
        assertEquals("arun@test.com", response.getEmail());
        assertEquals("USER",         response.getRole());
        assertTrue(response.isVerified());
    }

    @Test
    @DisplayName("getProfile: should NOT call userRepository")
    void getProfile_shouldNotCallRepository() {
        userService.getProfile(user);

        // getProfile just maps the already-loaded user — no DB call needed
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getProfile: unverified user should return isVerified=false")
    void getProfile_unverifiedUser_shouldReturnFalse() {
        User unverifiedUser = User.builder()
                .name("New User")
                .email("new@test.com")
                .password("pass")
                .role(Role.USER)
                .isVerified(false)
                .build();
        ReflectionTestUtils.setField(unverifiedUser, "id", 2L);

        UserProfileResponse response = userService.getProfile(unverifiedUser);

        assertFalse(response.isVerified());
    }

    @Test
    @DisplayName("getProfile: ADMIN user should return role ADMIN")
    void getProfile_adminUser_shouldReturnAdminRole() {
        User adminUser = User.builder()
                .name("Admin")
                .email("admin@test.com")
                .password("pass")
                .role(Role.ADMIN)
                .isVerified(true)
                .build();
        ReflectionTestUtils.setField(adminUser, "id", 3L);

        UserProfileResponse response = userService.getProfile(adminUser);

        assertEquals("ADMIN", response.getRole());
    }

    // ── updateProfile() ───────────────────────────────────────────

    @Test
    @DisplayName("updateProfile: should update name and return response")
    void updateProfile_shouldUpdateName() {
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse response = userService.updateProfile(user, updateRequest);

        assertNotNull(response);
        assertEquals("Arun K", response.getName());
        assertEquals("Arun K", user.getName()); // entity mutated
    }

    @Test
    @DisplayName("updateProfile: should call userRepository.save() once")
    void updateProfile_shouldCallSaveOnce() {
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(user, updateRequest);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("updateProfile: should not change email or role")
    void updateProfile_shouldNotChangeEmailOrRole() {
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(user, updateRequest);

        // Email and role must remain unchanged
        assertEquals("arun@test.com", user.getEmail());
        assertEquals(Role.USER,       user.getRole());
    }

    // ── changePassword() ──────────────────────────────────────────

    @Test
    @DisplayName("changePassword: correct current password should update successfully")
    void changePassword_correctCurrentPassword_shouldUpdate() {
        when(passwordEncoder.matches("oldPassword123", "encoded_old_password"))
                .thenReturn(true);
        when(passwordEncoder.matches("newPassword456", "encoded_old_password"))
                .thenReturn(false);
        when(passwordEncoder.encode("newPassword456"))
                .thenReturn("encoded_new_password");

        assertDoesNotThrow(() ->
                userService.changePassword(user, changePasswordRequest));

        verify(userRepository, times(1)).save(user);
        assertEquals("encoded_new_password", user.getPassword());
    }

    @Test
    @DisplayName("changePassword: wrong current password should throw IllegalArgumentException")
    void changePassword_wrongCurrentPassword_shouldThrowException() {
        when(passwordEncoder.matches("oldPassword123", "encoded_old_password"))
                .thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(user, changePasswordRequest));

        assertEquals("Current password is incorrect", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword: new password same as current should throw IllegalArgumentException")
    void changePassword_samePassword_shouldThrowException() {
        // Current password matches ✅
        when(passwordEncoder.matches("oldPassword123", "encoded_old_password"))
                .thenReturn(true);
        // New password ALSO matches current → reject
        when(passwordEncoder.matches("newPassword456", "encoded_old_password"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(user, changePasswordRequest));

        assertEquals("New password must be different from current password",
                ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword: should encode new password before saving")
    void changePassword_shouldEncodeNewPassword() {
        when(passwordEncoder.matches("oldPassword123", "encoded_old_password"))
                .thenReturn(true);
        when(passwordEncoder.matches("newPassword456", "encoded_old_password"))
                .thenReturn(false);
        when(passwordEncoder.encode("newPassword456"))
                .thenReturn("encoded_new_password");

        userService.changePassword(user, changePasswordRequest);

        // Raw new password must never be stored
        verify(passwordEncoder, times(1)).encode("newPassword456");
        assertNotEquals("newPassword456", user.getPassword());
    }

    @Test
    @DisplayName("changePassword: should save user after successful password change")
    void changePassword_shouldSaveUserOnce() {
        when(passwordEncoder.matches("oldPassword123", "encoded_old_password"))
                .thenReturn(true);
        when(passwordEncoder.matches("newPassword456", "encoded_old_password"))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded_new_password");

        userService.changePassword(user, changePasswordRequest);

        verify(userRepository, times(1)).save(user);
    }
}
