package com.shopping.cart.mapper;

import com.shopping.cart.dto.request.UpdateProfileRequest;
import com.shopping.cart.entity.Profile;

public class ProfileMapper {
    public static Profile fromUpdateProfileRequest(Profile profile, UpdateProfileRequest updateProfileRequest) {
        profile.setAddress1(updateProfileRequest.getAddress1() != null ? updateProfileRequest.getAddress1() : "");
        profile.setAddress2(updateProfileRequest.getAddress2() != null ? updateProfileRequest.getAddress2() : "");
        profile.setUnit(updateProfileRequest.getUnit() != null ? updateProfileRequest.getUnit() : "");
        profile.setFloor(updateProfileRequest.getFloor() != null ? updateProfileRequest.getFloor() : "");
        profile.setCity(updateProfileRequest.getCity() != null ? updateProfileRequest.getCity() : "");
        profile.setState(updateProfileRequest.getState() != null ? updateProfileRequest.getState() : "");
        profile.setCountry(updateProfileRequest.getCountry() != null ? updateProfileRequest.getCountry() : "");
        profile.setZipCode(updateProfileRequest.getZipCode() != null ? updateProfileRequest.getZipCode() : "");
        return profile;
    }
}
