package com.demo.controller;

import com.demo.model.entity.MentoringSession;
import com.demo.model.entity.User;
import com.demo.service.EquipmentService;
import com.demo.service.MentoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/lecturer")
public class LecturerController {

    @Autowired
    private MentoringService mentoringService;

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping("/home")
    public String lecturerHome(HttpSession session, Model model) {
        User user = (User) session.getAttribute("userSession");

        if (user == null || !"LECTURER".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<MentoringSession> mySchedules = mentoringService.getByLecturer(user);
        model.addAttribute("mySchedules", mySchedules);

        return "lecturer/home";
    }

    @GetMapping("/mentoring/add")
    public String showAddForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("userSession");
        if (user == null || !"LECTURER".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("sessionData", new MentoringSession());
        model.addAttribute("equipments", equipmentService.getAll());

        return "lecturer/create_schedule";
    }

    @PostMapping("/mentoring/save")
    public String saveSession(@ModelAttribute MentoringSession mSession,
                              @RequestParam(required = false) Long equipmentId,
                              HttpSession session) {
        User lecturer = (User) session.getAttribute("userSession");

        if (lecturer == null || !"LECTURER".equals(lecturer.getRole())) {
            return "redirect:/login";
        }


        mSession.setLecturer(lecturer);
        mSession.setStatus("AVAILABLE");

        if (lecturer.getLecturer() != null && lecturer.getLecturer().getLabRoom() != null) {
            mSession.setLabRoom(lecturer.getLecturer().getLabRoom());
        }

        if (equipmentId != null) {
            mSession.setEquipment(equipmentService.getById(equipmentId));
        }

        mentoringService.save(mSession);

        return "redirect:/lecturer/home";
    }
}