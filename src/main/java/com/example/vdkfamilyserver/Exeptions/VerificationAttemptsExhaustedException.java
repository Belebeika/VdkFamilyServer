package com.example.vdkfamilyserver.Exeptions;

public class VerificationAttemptsExhaustedException extends RuntimeException {
    public VerificationAttemptsExhaustedException() {
        super("No verification attempts left");
    }
}
