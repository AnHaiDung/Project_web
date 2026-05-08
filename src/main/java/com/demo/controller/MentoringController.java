package com.demo.controller;

import com.demo.model.dto.BookingDTO;
import com.demo.model.entity.User;
import com.demo.repository.DepartmentRepository;
import com.demo.repository.UserRepository;
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
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/student/add")
    public String studentAddForm(@RequestParam(required = false) Long departmentId,
                                 Model model,
                                 HttpSession session) {
        User user = (User) session.getAttribute("userSession");
        if (user == null || !"STUDENT".equals(user.getRole())) return "redirect:/login";

        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setDepartmentId(departmentId);

        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("lecturers", departmentId == null ? List.of() : userRepository.findLecturersByDepartmentId(departmentId));
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("bookingDTO", bookingDTO);

        return "student/mentoring_form";
    }

    @PostMapping("/student/book")
    public String studentBook(@ModelAttribute BookingDTO bookingDTO,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        User student = (User) session.getAttribute("userSession");
        if (student == null || !"STUDENT".equals(student.getRole())) return "redirect:/login";

        try {
            mentoringService.bookSession(student, bookingDTO);
            redirectAttributes.addFlashAttribute("msgSuccess", "Đặt lịch thành công. Phiếu đang chờ giảng viên xác nhận");
            return "redirect:/student/home";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("msgError", ex.getMessage());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("lecturers", bookingDTO.getDepartmentId() == null ? List.of() : userRepository.findLecturersByDepartmentId(bookingDTO.getDepartmentId()));
            model.addAttribute("selectedDepartmentId", bookingDTO.getDepartmentId());
            model.addAttribute("bookingDTO", bookingDTO);
            return "student/mentoring_form";
        }
    }
}
