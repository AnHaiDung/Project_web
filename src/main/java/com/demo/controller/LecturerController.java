package com.demo.controller;

import com.demo.model.dto.EvaluationDTO;
import com.demo.model.entity.User;
import com.demo.service.EquipmentService;
import com.demo.service.MentoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lecturer")
public class LecturerController {

    @Autowired
    private MentoringService mentoringService;

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("userSession");
        if (user == null || !"LECTURER".equals(user.getRole())) return "redirect:/login";

        model.addAttribute("pendingSessions", mentoringService.getPendingByLecturer(user));
        model.addAttribute("allSessions", mentoringService.getByLecturer(user));
        model.addAttribute("equipments", equipmentService.getAll());
        model.addAttribute("evaluationDTO", new EvaluationDTO());

        return "lecturer/home";
    }

    @PostMapping("/complete")
    public String complete(@ModelAttribute EvaluationDTO evaluationDTO,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User lecturer = (User) session.getAttribute("userSession");
        if (lecturer == null || !"LECTURER".equals(lecturer.getRole())) return "redirect:/login";

        try {
            mentoringService.completeConsultation(lecturer, evaluationDTO);
            redirectAttributes.addFlashAttribute("msgSuccess", "Đã lưu đánh giá và tạo phiếu chờ cấp phát");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("msgError", ex.getMessage());
        }

        return "redirect:/lecturer/home";
    }

    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        mentoringService.updateStatus(id, "REJECTED");
        redirectAttributes.addFlashAttribute("msgSuccess", "Đã từ chối lịch tư vấn");
        return "redirect:/lecturer/home";
    }
}
