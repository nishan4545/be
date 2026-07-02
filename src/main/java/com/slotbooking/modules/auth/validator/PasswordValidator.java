package com.slotbooking.modules.auth.validator;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    public boolean isValid(String password) {
        return password != null && password.length() >= 8;
    }
}
