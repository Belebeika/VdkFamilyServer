package com.example.vdkfamilyserver.Controllers;

import com.example.vdkfamilyserver.DTO.Data.ChildDTO;
import com.example.vdkfamilyserver.Models.Child;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Services.ChildService;
import com.example.vdkfamilyserver.Annotations.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService childService;

    @PostMapping
    public ResponseEntity<ChildDTO> addChild(
            @CurrentUser User user,
            @RequestBody Child child) {
        Child saved = childService.addChildToUser(user.getId(), child);
        return ResponseEntity.ok(new ChildDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<ChildDTO>> getUserChildren(
            @CurrentUser User user) {
        List<ChildDTO> children = childService.getUserChildren(user.getId())
                .stream()
                .map(ChildDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(children);
    }

    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> removeChild(
            @CurrentUser User user,
            @PathVariable Long childId) {
        childService.removeChild(user.getId(), childId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{childId}")
    public ResponseEntity<ChildDTO> updateChild(
            @CurrentUser User user,
            @PathVariable Long childId,
            @RequestBody Child child) {
        Child updated = childService.updateChild(user.getId(), childId, child);
        return ResponseEntity.ok(new ChildDTO(updated));
    }
}
