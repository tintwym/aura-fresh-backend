package com.shopping.cart.service;

import com.shopping.cart.dto.request.UpdateProfileRequest;
import com.shopping.cart.entity.Profile;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IProfileService;
import com.shopping.cart.mapper.ProfileMapper;
import com.shopping.cart.repository.ProfileRepository;
import com.shopping.cart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ProfileService implements IProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ProfileService(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            UserService userService) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public Profile getProfile(String token) {
        User user = userService.requireUser(token);
        return profileRepository.findByUserId(user.getId());
    }

    @Override
    @Transactional
    public void updateProfile(String token, UpdateProfileRequest updateProfileRequest) {
        User user = userService.requireUser(token);
        Profile profile = profileRepository.findByUserId(user.getId());

        if (profile == null) {
            profile = new Profile();
            user.setProfile(profile);
        }

        Profile updatedProfile = ProfileMapper.fromUpdateProfileRequest(profile, updateProfileRequest);
        profileRepository.save(Objects.requireNonNull(updatedProfile));
        user.setProfile(updatedProfile);
        userRepository.save(user);
    }
}
