package com.demo.service;

import com.demo.model.entity.Equipment;
import com.demo.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EquipmentService {
    @Autowired
    private EquipmentRepository equipmentRepository;

    public List<Equipment> getAll() {
        return equipmentRepository.findAll();
    }

    public void save(Equipment equipment) {
        equipmentRepository.save(equipment);
    }
    public Equipment getById(Long id) {
        return equipmentRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        equipmentRepository.deleteById(id);
    }
}