package com.demo.controller;

import com.demo.model.entity.*;
import com.demo.repository.*;
import com.demo.service.MentoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/student/mentoring")
public class MentoringController {

    @Autowired
    private MentoringService mentoringService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/add")
    public String addForm(@RequestParam(required = false) Long deptId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("userSession");
        if (user == null || !"STUDENT".equals(user.getRole())) return "redirect:/login";

        model.addAttribute("departments", departmentRepository.findAll());

        if (deptId != null) {
            List<User> lecturers = userRepository.findAll().stream()
                    .filter(u -> "LECTURER".equals(u.getRole())
                            && u.getLecturer() != null
                            && u.getLecturer().getDepartment().getId().equals(deptId))
                    .toList();
            model.addAttribute("lecturers", lecturers);
        }

        model.addAttribute("sessionData", new MentoringSession());
        return "student/mentoring_form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute MentoringSession mSession, HttpSession session) {
        User student = (User) session.getAttribute("userSession");
        if (student == null) return "redirect:/login";

        User selectedLecturer = userRepository.findById(mSession.getLecturer().getId()).orElse(null);
        if (selectedLecturer != null && selectedLecturer.getLecturer() != null) {
            mSession.setLabRoom(selectedLecturer.getLecturer().getLabRoom());
        }

        mSession.setStudent(student);
        mSession.setStartTime(new Date());
        mSession.setStatus("PENDING");

        mentoringService.save(mSession);
        return "redirect:/student/home";
    }
}