package com.example.vdkfamilyserver.Controllers;

import com.example.vdkfamilyserver.Models.Child;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Services.ChildService;
import com.example.vdkfamilyserver.Annotations.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService childService;

    @PostMapping
    public ResponseEntity<Child> addChild(
            @CurrentUser User user,
            @RequestBody Child child) {
        return ResponseEntity.ok(childService.addChildToUser(user.getId(), child));
    }

    @GetMapping
    public ResponseEntity<Set<Child>> getUserChildren(
            @CurrentUser User user) {
        return ResponseEntity.ok(childService.getUserChildren(user.getId()));
    }

    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> removeChild(
            @CurrentUser User user,
            @PathVariable Long childId) {
        childService.removeChild(user.getId(), childId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{childId}")
    public ResponseEntity<Child> updateChild(
            @CurrentUser User user,
            @PathVariable Long childId,
            @RequestBody Child child) {
        return ResponseEntity.ok(childService.updateChild(user.getId(), childId, child));
    }
}