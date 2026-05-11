package com.demo.controller;

import com.demo.model.entity.User;
import com.demo.service.MentoringService;
import com.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        boolean hasError = false;

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("usernameError", "Tên đăng nhập không được để trống");
            hasError = true;
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("passwordError", "Mật khẩu không được để trống");
            hasError = true;
        }

        if (hasError) {
            model.addAttribute("username", username);
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
        model.addAttribute("username", username);
        return "login";
    }

    @GetMapping("/admin/home")
    public String adminHome(HttpSession session, Model model) {
        if (isNotRole(session, "ADMIN")) return "redirect:/login";

        model.addAttribute("equipmentStats", mentoringService.getBorrowedEquipmentStats());
        model.addAttribute("topLecturers", mentoringService.getTopLecturers());

        return "admin/home";
    }

    @GetMapping("/student/home")
    public String studentHome(HttpSession session, Model model) {
        User user = (User) session.getAttribute("userSession");

        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("mySessions", mentoringService.getByStudent(user));
        return "student/home";
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model model) {
        boolean hasError = false;

        if (user.getProfile() == null ||
                user.getProfile().getFullName() == null ||
                user.getProfile().getFullName().trim().isEmpty()) {
            model.addAttribute("fullNameError", "Họ tên không được để trống");
            hasError = true;
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            model.addAttribute("usernameError", "Tên đăng nhập không được để trống");
            hasError = true;
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            model.addAttribute("passwordError", "Mật khẩu không được để trống");
            hasError = true;
        } else if (user.getPassword().trim().length() < 6) {
            model.addAttribute("passwordError", "Mật khẩu phải có ít nhất 6 ký tự");
            hasError = true;
        }

        if (user.getUsername() != null &&
                !user.getUsername().trim().isEmpty() &&
                userService.isUsernameExist(user.getUsername())) {
            model.addAttribute("usernameError", "Tên đăng nhập này đã tồn tại");
            hasError = true;
        }

        if (hasError) {
            model.addAttribute("user", user);
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

    private boolean isNotRole(HttpSession session, String role) {
        User user = (User) session.getAttribute("userSession");
        return user == null || !role.equals(user.getRole());
    }

    @PostMapping("/student/sessions/cancel/{id}")
    public String cancelSession(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("userSession");

        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            mentoringService.cancelByStudent(id, user);
            redirectAttributes.addFlashAttribute("msgSuccess", "Hủy lịch thành công");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("msgError", ex.getMessage());
        }

        return "redirect:/student/home";
    }
}
