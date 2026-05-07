package com.demo.controller;

import com.demo.model.entity.*;
import com.demo.repository.*;
import com.demo.service.EquipmentService;
import com.demo.service.MentoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/mentoring") // Đổi lại mapping dùng chung
public class MentoringController {

    @Autowired
    private MentoringService mentoringService;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private MentoringSessionRepository mentoringSessionRepository;

    @GetMapping("/lecturer/add")
    public String lecturerAddForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("userSession");
        if (user == null || !"LECTURER".equals(user.getRole())) return "redirect:/login";

        model.addAttribute("equipments", equipmentService.getAll());
        model.addAttribute("sessionData", new MentoringSession());
        return "lecturer/create_schedule";
    }

    @PostMapping("/lecturer/save")
    public String lecturerSave(@ModelAttribute MentoringSession mSession,
                               @RequestParam Long equipmentId,
                               HttpSession session) {
        User lecturer = (User) session.getAttribute("userSession");

        mSession.setLecturer(lecturer);
        mSession.setLabRoom(lecturer.getLecturer().getLabRoom());
        mSession.setStatus("AVAILABLE");

        if (equipmentId != null) {
            mSession.setEquipment(equipmentService.getById(equipmentId));
        }

        mentoringService.save(mSession);
        return "redirect:/lecturer/home";
    }

    @GetMapping("/student/add")
    public String studentAddForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("userSession");
        if (user == null || !"STUDENT".equals(user.getRole())) return "redirect:/login";

        List<MentoringSession> availableSessions = mentoringSessionRepository.findAll().stream()
                .filter(s -> "AVAILABLE".equals(s.getStatus()))
                .toList();

        model.addAttribute("availableSessions", availableSessions);
        return "student/mentoring_form";
    }

    @PostMapping("/student/register")
    public String studentRegister(@RequestParam Long sessionId, HttpSession session) {
        User student = (User) session.getAttribute("userSession");
        MentoringSession mSession = mentoringService.getById(sessionId);

        if (mSession != null && student != null) {
            mSession.setStudent(student);
            mSession.setStatus("PENDING");
            mentoringService.save(mSession);
        }
        return "redirect:/student/home";
    }
}