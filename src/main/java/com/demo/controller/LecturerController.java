package com.demo.controller;

import com.demo.model.entity.User;
import com.demo.service.MentoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lecturer")
public class LecturerController {

    @Autowired
    private MentoringService mentoringService;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("userSession");
        if (user == null || !"LECTURER".equals(user.getRole())) return "redirect:/login";

        model.addAttribute("sessions", mentoringService.getByLecturer(user));
        return "lecturer/home";
    }

    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        mentoringService.updateStatus(id, "APPROVED");
        return "redirect:/lecturer/home";
    }

    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id) {
        mentoringService.updateStatus(id, "REJECTED");
        return "redirect:/lecturer/home";
    }
}