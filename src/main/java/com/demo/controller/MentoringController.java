package com.demo.controller;

import com.demo.model.entity.MentoringSession;
import com.demo.model.entity.User;
import com.demo.repository.DepartmentRepository;
import com.demo.repository.MentoringSessionRepository;
import com.demo.service.EquipmentService;
import com.demo.service.MentoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mentoring")
public class MentoringController {

    @Autowired
    private MentoringService mentoringService;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private MentoringSessionRepository mentoringSessionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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
                               @RequestParam(required = false) Long equipmentId,
                               HttpSession session) {
        User lecturer = (User) session.getAttribute("userSession");
        if (lecturer == null || !"LECTURER".equals(lecturer.getRole())) return "redirect:/login";

        mSession.setLecturer(lecturer);

        if (lecturer.getLecturer() != null && lecturer.getLecturer().getLabRoom() != null) {
            mSession.setLabRoom(lecturer.getLecturer().getLabRoom());
        }

        mSession.setStatus("AVAILABLE");

        if (equipmentId != null) {
            mSession.setEquipment(equipmentService.getById(equipmentId));
        }

        mentoringService.save(mSession);
        return "redirect:/lecturer/home";
    }

    @GetMapping("/student/add")
    public String studentAddForm(@RequestParam(required = false) Long departmentId,
                                 Model model,
                                 HttpSession session) {
        User user = (User) session.getAttribute("userSession");
        if (user == null || !"STUDENT".equals(user.getRole())) return "redirect:/login";

        List<MentoringSession> availableSessions = mentoringSessionRepository.findAll()
                .stream()
                .filter(s -> "AVAILABLE".equals(s.getStatus()))
                .filter(s -> departmentId == null ||
                        (
                                s.getLecturer() != null &&
                                        s.getLecturer().getLecturer() != null &&
                                        s.getLecturer().getLecturer().getDepartment() != null &&
                                        s.getLecturer().getLecturer().getDepartment().getId().equals(departmentId)
                        ))
                .toList();

        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("availableSessions", availableSessions);

        return "student/mentoring_form";
    }

    @PostMapping("/student/register")
    public String studentRegister(@RequestParam Long sessionId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User student = (User) session.getAttribute("userSession");
        if (student == null || !"STUDENT".equals(student.getRole())) return "redirect:/login";

        MentoringSession mSession = mentoringService.getById(sessionId);

        if (mSession == null) {
            redirectAttributes.addFlashAttribute("msgError", "Lịch tư vấn không tồn tại");
            return "redirect:/mentoring/student/add";
        }

        if (!"AVAILABLE".equals(mSession.getStatus())) {
            redirectAttributes.addFlashAttribute("msgError", "Lịch này đã có sinh viên đăng ký");
            return "redirect:/mentoring/student/add";
        }

        mSession.setStudent(student);
        mSession.setStatus("PENDING");
        mentoringService.save(mSession);

        redirectAttributes.addFlashAttribute("msgSuccess", "Tạo phiếu đăng ký thành công");
        return "redirect:/student/home";
    }
}
