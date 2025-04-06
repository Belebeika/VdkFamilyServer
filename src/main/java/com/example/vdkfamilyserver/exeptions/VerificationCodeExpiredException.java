package com.example.vdkfamilyserver.exeptions;

public class VerificationCodeExpiredException extends RuntimeException {
    public VerificationCodeExpiredException() {
        super("Verification code expired");
    }
}
