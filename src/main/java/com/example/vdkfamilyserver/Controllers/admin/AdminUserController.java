package com.example.vdkfamilyserver.Controllers.admin;

import com.example.vdkfamilyserver.DTO.Admin.UserEdit;
import com.example.vdkfamilyserver.Models.User;
import com.example.vdkfamilyserver.Repositories.UserRepository;
import com.example.vdkfamilyserver.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @GetMapping
    public String listUsers(@RequestParam(name = "phone", required = false) String phone,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<User> userPage;

        if (phone != null && !phone.isBlank()) {
            userPage = userRepository.findByPhoneNumberContainingIgnoreCase(phone, pageable);
            model.addAttribute("search", phone);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("currentPage", page);

        return "admin/userboard/users";
    }

    @GetMapping("/new")
    public String addUser(Model model) {
        model.addAttribute("user", new User());
        return "admin/userboard/useradd";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User existingUser = userRepository.findByPhoneNumber(user.getPhoneNumber()).orElse(null);

        if (existingUser != null) {
            redirectAttributes.addFlashAttribute("error",
                    "Телефон уже занят другим пользователем: " +
                            "<a href='/admin/users/" + existingUser.getId() + "'>Редактировать</a>"
            );
            return "redirect:/admin/users/new";
        }

        userService.save(user);
        redirectAttributes.addFlashAttribute("message", "Пользователь успешно добавлен");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow();
        UserEdit dto = new UserEdit();

        // Копируем данные в DTO
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPatronymic(user.getPatronymic());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setMarried(user.isMarried());

        model.addAttribute("user", dto);
        return "admin/userboard/useredit";
    }

    @PostMapping("/update")
    public String updateUser(UserEdit dto, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(dto.getId()).orElseThrow();

        // Проверка на уникальность номера телефона
        User existingUser = userRepository.findByPhoneNumber(dto.getPhoneNumber()).orElse(null);

        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            // Телефон занят другим пользователем
            redirectAttributes.addFlashAttribute("error",
                    "Телефон уже занят другим пользователем: " +
                            "<a href='/admin/users/" + existingUser.getId() + "'>Редактировать</a>"
            );
            return "redirect:/admin/users" + user.getId();
        }

        // Всё ок — обновляем
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPatronymic(dto.getPatronymic());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(dto.getRole());
        user.setActive(dto.isActive());
        user.setMarried(dto.isMarried());

        redirectAttributes.addFlashAttribute("message", "Пользователь успешно обновлён");
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Пользователь удалён");
        return "redirect:/admin/users";
    }
}
