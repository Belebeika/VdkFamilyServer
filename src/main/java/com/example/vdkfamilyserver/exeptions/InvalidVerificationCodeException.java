package com.example.vdkfamilyserver.exeptions;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException() {
        super("Invalid verification code");
    }
}
