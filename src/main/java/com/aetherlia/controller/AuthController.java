package com.aetherlia.controller;

import com.aetherlia.entity.User;
import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {


private final UserService userService;
private final PasswordEncoder passwordEncoder;
private final UserRepository userRepository;
private final PlayerMonsterRepository playerMonsterRepository;

public AuthController(
        UserService userService,
        PasswordEncoder passwordEncoder,
        UserRepository userRepository,
        PlayerMonsterRepository playerMonsterRepository) {

    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.playerMonsterRepository = playerMonsterRepository;
}

// =====================================================
// TRANG ĐĂNG KÝ
// =====================================================

@GetMapping("/register")
public String showRegisterForm() {
    return "register";
}

// =====================================================
// XỬ LÝ ĐĂNG KÝ
// =====================================================

@PostMapping("/register")
public String register(User user) {

    user.setPassword(
            passwordEncoder.encode(
                    user.getPassword()
            )
    );

    userService.register(user);

    /*
     * Đăng ký xong KHÔNG vào starter ngay.
     * Chuyển về trang đăng nhập trước.
     */
    return "redirect:/login?registered";
}

// =====================================================
// SAU KHI ĐĂNG NHẬP
// =====================================================

@GetMapping("/after-login")
public String afterLogin(
        org.springframework.security.core.Authentication authentication,
        HttpSession session) {

    if (authentication == null
            || !authentication.isAuthenticated()
            || "anonymousUser".equals(
                    authentication.getName()
            )) {

        return "redirect:/login";
    }

    String username =
            authentication.getName();

    User user =
            userRepository.findByUsername(username);

    if (user == null) {
        return "redirect:/login";
    }

    /*
     * Kiểm tra tài khoản đã có Pokémon chưa.
     */
    boolean hasMonster =
            !playerMonsterRepository
                    .findByUser(user)
                    .isEmpty();

    if (!hasMonster) {
        return "redirect:/starter";
    }

    return "redirect:/dashboard";
}


}
