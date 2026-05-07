package com.demo.controller;

import com.demo.model.entity.Equipment;
import com.demo.model.entity.User;
import com.demo.service.EquipmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private EquipmentService equipmentService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("userSession");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/equipments")
    public String listEquipments(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("equipments", equipmentService.getAll());
        return "admin/equipment_list";
    }

    @GetMapping("/equipments/add")
    public String addForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("equipment", new Equipment());
        return "admin/equipment_form";
    }

    @PostMapping("/equipments/save")
    public String saveEquipment(@ModelAttribute Equipment equipment, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        equipmentService.save(equipment);
        return "redirect:/admin/equipments";
    }

    @GetMapping("/equipments/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("equipment", equipmentService.getById(id));
        return "admin/equipment_form";
    }

    @GetMapping("/equipments/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        equipmentService.delete(id);
        return "redirect:/admin/equipments";
    }
}