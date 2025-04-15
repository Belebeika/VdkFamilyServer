package com.example.vdkfamilyserver.Controllers;

import com.example.vdkfamilyserver.DTO.ProfileToClient;
import com.example.vdkfamilyserver.DTO.UpdateUser;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Services.UserService;
import com.example.vdkfamilyserver.annotations.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get")
    public ProfileToClient getUser(@CurrentUser User user) {
        ProfileToClient profile = new ProfileToClient();
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setPatronymic(user.getPatronymic());
        profile.setMarried(user.isMarried());
        return profile;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@CurrentUser User user, @Valid UpdateUser newuserinfo) {
        try {
            userService.update(user, newuserinfo);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Не получилось обновить данные");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
