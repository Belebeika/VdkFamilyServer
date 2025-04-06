package com.example.vdkfamilyserver.exeptions;

public class TempUserNotFoundException extends RuntimeException {
    public TempUserNotFoundException(String phone) {
        super("Temp user not found for phone: " + phone);
    }
}

