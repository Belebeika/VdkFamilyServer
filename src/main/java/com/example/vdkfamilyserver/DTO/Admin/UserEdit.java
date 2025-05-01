package com.example.vdkfamilyserver.DTO.Admin;

import com.example.vdkfamilyserver.Models.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserEdit {
    private Long id;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phoneNumber;
    private User.Role role;
    private boolean active;
    private boolean isMarried;
}