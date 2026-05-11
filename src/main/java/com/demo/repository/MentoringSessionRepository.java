package com.demo.repository;

import com.demo.model.entity.MentoringSession;
import com.demo.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {
    List<MentoringSession> findByStudent(User student);
    List<MentoringSession> findByLecturer(User lecturer);
    List<MentoringSession> findByLecturerAndStatus(User lecturer, String status);
    boolean existsByLecturerAndStartTimeAndStatusIn(User lecturer, Date startTime, List<String> statuses);

    @Query("""
        select coalesce(p.fullName, l.username), count(s.id)
        from MentoringSession s
        join s.lecturer l
        left join l.profile p
        where s.status in ('WAITING_ALLOCATION', 'EXPORTED')
        group by l.id, p.fullName, l.username
        order by count(s.id) desc
    """)
    List<Object[]> getTopLecturers(Pageable pageable);
}
