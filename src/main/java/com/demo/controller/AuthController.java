package com.demo.controller;

import com.demo.model.entity.User;
import com.demo.service.MentoringService;
import com.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private MentoringService mentoringService;

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          Model model,
                          HttpSession session) {

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("msgError", "Vui lòng không để trống tài khoản/mật khẩu  ");
            return "login";
        }

        User user = userService.checkLogin(username, password);
        if (user != null) {
            session.setAttribute("userSession", user);
            if ("ADMIN".equals(user.getRole())) return "redirect:/admin/home";
            if ("LECTURER".equals(user.getRole())) return "redirect:/lecturer/home";
            return "redirect:/student/home";
        }

        model.addAttribute("msgError", "Tài khoản hoặc mật khẩu không chính xác");
        return "login";
    }

    @GetMapping("/admin/home")
    public String adminHome(HttpSession session) {
        if (isNotRole(session, "ADMIN")) return "redirect:/login";
        return "admin/home";
    }

    @GetMapping("/lecturer/home")
    public String lecturerHome(HttpSession session) {
        if (isNotRole(session, "LECTURER")) return "redirect:/login";
        return "lecturer/home";
    }

    @GetMapping("/student/home")
    public String studentHome(HttpSession session, Model model) { // Thêm Model vào tham số
        User user = (User) session.getAttribute("userSession");

        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("mySessions", mentoringService.getByStudent(user));

        return "student/home";
    }

    private boolean isNotRole(HttpSession session, String role) {
        User user = (User) session.getAttribute("userSession");
        return user == null || !role.equals(user.getRole());
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model model) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            model.addAttribute("msgError", "Tên đăng nhập không được để trống");
            return "register";
        }

        if (userService.isUsernameExist(user.getUsername())) {
            model.addAttribute("msgError", "Tên đăng nhập này đã tồn tại");
            return "register";
        }

        userService.register(user);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}