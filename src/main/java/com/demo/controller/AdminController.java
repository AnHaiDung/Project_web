package com.demo.controller;

import com.demo.model.entity.Equipment;
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
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private MentoringService mentoringService;

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
        model.addAttribute("quantityInput", "");
        return "admin/equipment_form";
    }

    @PostMapping("/equipments/save")
    public String saveEquipment(@RequestParam(required = false) Long id,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String quantityInput,
                                HttpSession session,
                                Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        boolean hasError = false;
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setName(name);
        equipment.setDescription(description);

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "Tên thiết bị không được để trống");
            hasError = true;
        }

        if (description == null || description.trim().isEmpty()) {
            model.addAttribute("descriptionError", "Mô tả không được để trống");
            hasError = true;
        }

        if (quantityInput == null || quantityInput.trim().isEmpty()) {
            model.addAttribute("quantityError", "Số lượng tồn kho không được để trống");
            hasError = true;
        } else {
            try {
                int quantity = Integer.parseInt(quantityInput.trim());
                if (quantity < 0) {
                    model.addAttribute("quantityError", "Số lượng tồn kho không được âm");
                    hasError = true;
                } else {
                    equipment.setQuantity(quantity);
                }
            } catch (NumberFormatException ex) {
                model.addAttribute("quantityError", "Số lượng tồn kho phải là số");
                hasError = true;
            }
        }

        if (hasError) {
            model.addAttribute("equipment", equipment);
            model.addAttribute("quantityInput", quantityInput);
            return "admin/equipment_form";
        }

        equipmentService.save(equipment);
        return "redirect:/admin/equipments";
    }

    @GetMapping("/equipments/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        Equipment equipment = equipmentService.getById(id);
        model.addAttribute("equipment", equipment);
        model.addAttribute("quantityInput", equipment != null ? String.valueOf(equipment.getQuantity()) : "");
        return "admin/equipment_form";
    }

    @GetMapping("/equipments/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        equipmentService.delete(id);
        return "redirect:/admin/equipments";
    }

    @GetMapping("/borrowings")
    public String waitingBorrowings(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("borrowings", mentoringService.getWaitingAllocationRecords());
        return "admin/borrowing_list";
    }

    @PostMapping("/borrowings/export/{id}")
    public String confirmExport(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";

        try {
            mentoringService.confirmExport(id);
            redirectAttributes.addFlashAttribute("msgSuccess", "Xuất kho thành công");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("msgError", ex.getMessage());
        }

        return "redirect:/admin/borrowings";
    }
}
