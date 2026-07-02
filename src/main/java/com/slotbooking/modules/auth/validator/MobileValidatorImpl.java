package com.slotbooking.modules.auth.validator;

import org.springframework.stereotype.Component;

@Component
public class MobileValidatorImpl implements MobileValidator {
    @Override
    public boolean isValid(String mobileNumber) {
        return mobileNumber != null && mobileNumber.matches("^[0-9]{10}$");
    }
}
