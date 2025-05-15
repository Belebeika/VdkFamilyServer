package com.example.vdkfamilyserver.Exeptions;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super("This user not found");
  }
}

