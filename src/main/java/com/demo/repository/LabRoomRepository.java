package com.demo.repository;

import com.demo.model.entity.LabRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabRoomRepository extends JpaRepository<LabRoom, Long> {
}