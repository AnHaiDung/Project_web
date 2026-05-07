package com.demo.repository;
import com.demo.model.entity.MentoringSession;
import com.demo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {
    List<MentoringSession> findByStudent(User student);
    List<MentoringSession> findByLecturer(User lecturer);
}