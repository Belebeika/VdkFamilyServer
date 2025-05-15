package com.example.vdkfamilyserver.Exeptions;

public class TempUserNotFoundException extends RuntimeException {
    public TempUserNotFoundException(String phone) {
        super("Temp user not found for phone: " + phone);
    }
}

