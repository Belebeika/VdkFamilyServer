package com.example.vdkfamilyserver.Services;

import com.example.vdkfamilyserver.Models.Child;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Repositories.ChildRepository;
import com.example.vdkfamilyserver.Repositories.UserRepository;
import com.example.vdkfamilyserver.exeptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChildService {
    private final ChildRepository childRepository;
    private final UserRepository userRepository;

    @Transactional
    public Child addChildToUser(Long userId, Child child) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        child.setParent(user);
        return childRepository.save(child);
    }

    @Transactional(readOnly = true)
    public Set<Child> getUserChildren(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return user.getChildren();
    }

    @Transactional
    public void removeChild(Long userId, Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        if (!child.getParent().getId().equals(userId)) {
            throw new IllegalArgumentException("Child does not belong to this user");
        }

        childRepository.delete(child);
    }

    @Transactional
    public Child updateChild(Long userId, Long childId, Child updatedChild) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        if (!child.getParent().getId().equals(userId)) {
            throw new IllegalArgumentException("Child does not belong to this user");
        }

        child.setBirthDate(updatedChild.getBirthDate());
        child.setGender(updatedChild.getGender());
        return childRepository.save(child);
    }
}