package com.example.vdkfamilyserver.exeptions;

public class VerificationAttemptsExhaustedException extends RuntimeException {
    public VerificationAttemptsExhaustedException() {
        super("No verification attempts left");
    }
}
