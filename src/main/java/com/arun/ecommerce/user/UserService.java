package com.arun.ecommerce.user;

import com.arun.ecommerce.entity.User;
import com.arun.ecommerce.user.dto.ChangePasswordRequest;
import com.arun.ecommerce.user.dto.UpdateProfileRequest;
import com.arun.ecommerce.user.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(User user);
    UserProfileResponse updateProfile(User user, UpdateProfileRequest request);
    void                changePassword(User user, ChangePasswordRequest request);
}
