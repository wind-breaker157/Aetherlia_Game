package com.aetherlia.controller;

import com.aetherlia.entity.User;
import com.aetherlia.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserService userService,
            PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(User user, Model model) {

        // Mã hóa password trước khi lưu
        user.setPassword(
            passwordEncoder.encode(user.getPassword())
        );

        userService.register(user);

        model.addAttribute(
            "message",
            "Đăng ký thành công!"
        );

        return "register";
    }
}