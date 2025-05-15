package com.example.vdkfamilyserver.Exeptions;

public class VerificationCodeExpiredException extends RuntimeException {
    public VerificationCodeExpiredException() {
        super("Verification code expired");
    }
}
