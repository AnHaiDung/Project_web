package com.demo.service;

import com.demo.model.entity.MentoringSession;
import com.demo.model.entity.User;
import com.demo.repository.MentoringSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MentoringService {

    @Autowired
    private MentoringSessionRepository mentoringSessionRepository;

    public void save(MentoringSession session) {
        mentoringSessionRepository.save(session);
    }

    public MentoringSession getById(Long id) {
        return mentoringSessionRepository.findById(id).orElse(null);
    }

    public List<MentoringSession> getByStudent(User student) {
        return mentoringSessionRepository.findByStudent(student);
    }

    public List<MentoringSession> getByLecturer(User lecturer) {
        return mentoringSessionRepository.findByLecturer(lecturer);
    }

    public void updateStatus(Long id, String status) {
        MentoringSession session = getById(id);
        if (session != null) {
            session.setStatus(status);
            mentoringSessionRepository.save(session);
        }
    }
}