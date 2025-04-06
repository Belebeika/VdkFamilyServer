package com.example.vdkfamilyserver.exeptions;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super("This user not found");
  }
}

