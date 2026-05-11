package com.demo.service;

import com.demo.model.dto.BookingDTO;
import com.demo.model.dto.EvaluationDTO;
import com.demo.model.entity.AcademicEvaluation;
import com.demo.model.entity.BorrowingDetail;
import com.demo.model.entity.BorrowingRecord;
import com.demo.model.entity.Equipment;
import com.demo.model.entity.MentoringSession;
import com.demo.model.entity.User;
import com.demo.repository.AcademicEvaluationRepository;
import com.demo.repository.BorrowingRecordRepository;
import com.demo.repository.EquipmentRepository;
import com.demo.repository.MentoringSessionRepository;
import com.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class MentoringService {

    @Autowired
    private MentoringSessionRepository mentoringSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private AcademicEvaluationRepository academicEvaluationRepository;

    @Autowired
    private BorrowingRecordRepository borrowingRecordRepository;

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

    public List<MentoringSession> getPendingByLecturer(User lecturer) {
        return mentoringSessionRepository.findByLecturerAndStatus(lecturer, "PENDING");
    }

    public List<BorrowingRecord> getWaitingAllocationRecords() {
        return borrowingRecordRepository.findByStatus("WAITING_ALLOCATION");
    }

    @Transactional
    public void bookSession(User student, BookingDTO bookingDTO) {
        if (student == null || !"STUDENT".equals(student.getRole())) {
            throw new IllegalArgumentException("Bạn cần đăng nhập bằng tài khoản sinh viên");
        }

        if (bookingDTO.getLecturerId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn giảng viên");
        }

        if (bookingDTO.getStartTime() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày giờ tư vấn");
        }

        if (!bookingDTO.getStartTime().after(new Date())) {
            throw new IllegalArgumentException("Không được đặt lịch trong quá khứ");
        }

        User lecturer = userRepository.findById(bookingDTO.getLecturerId())
                .orElseThrow(() -> new IllegalArgumentException("Giảng viên không tồn tại"));

        if (!"LECTURER".equals(lecturer.getRole())) {
            throw new IllegalArgumentException("Người được chọn không phải giảng viên");
        }

        List<String> activeStatuses = Arrays.asList("PENDING", "WAITING_ALLOCATION", "EXPORTED");
        boolean duplicated = mentoringSessionRepository.existsByLecturerAndStartTimeAndStatusIn(
                lecturer,
                bookingDTO.getStartTime(),
                activeStatuses
        );

        if (duplicated) {
            throw new IllegalArgumentException("Giảng viên đã có lịch ở khung giờ này");
        }

        MentoringSession mentoringSession = new MentoringSession();
        mentoringSession.setStudent(student);
        mentoringSession.setLecturer(lecturer);
        mentoringSession.setStartTime(bookingDTO.getStartTime());
        mentoringSession.setTopic(bookingDTO.getTopic());
        mentoringSession.setStatus("PENDING");

        mentoringSessionRepository.save(mentoringSession);
    }

    @Transactional
    public void completeConsultation(User lecturer, EvaluationDTO evaluationDTO) {
        if (lecturer == null || !"LECTURER".equals(lecturer.getRole())) {
            throw new IllegalArgumentException("Bạn cần đăng nhập bằng tài khoản giảng viên");
        }

        MentoringSession mentoringSession = getById(evaluationDTO.getSessionId());
        if (mentoringSession == null) {
            throw new IllegalArgumentException("Lịch tư vấn không tồn tại");
        }

        if (mentoringSession.getLecturer() == null || !mentoringSession.getLecturer().getId().equals(lecturer.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xử lý lịch này");
        }

        if (!"PENDING".equals(mentoringSession.getStatus())) {
            throw new IllegalArgumentException("Chỉ xử lý được lịch đang chờ xác nhận");
        }

        AcademicEvaluation evaluation = AcademicEvaluation.builder()
                .session(mentoringSession)
                .student(mentoringSession.getStudent())
                .lecturer(lecturer)
                .competencyLevel(evaluationDTO.getCompetencyLevel())
                .comment(evaluationDTO.getComment())
                .build();
        academicEvaluationRepository.save(evaluation);

        BorrowingRecord borrowingRecord = BorrowingRecord.builder()
                .session(mentoringSession)
                .student(mentoringSession.getStudent())
                .status("WAITING_ALLOCATION")
                .createdAt(new Date())
                .build();

        if (evaluationDTO.getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(evaluationDTO.getEquipmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Thiết bị không tồn tại"));

            int quantity = evaluationDTO.getQuantity() == null ? 1 : evaluationDTO.getQuantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng thiết bị phải lớn hơn 0");
            }

            BorrowingDetail detail = BorrowingDetail.builder()
                    .borrowingRecord(borrowingRecord)
                    .equipment(equipment)
                    .quantity(quantity)
                    .build();
            borrowingRecord.getDetails().add(detail);
        }

        borrowingRecordRepository.save(borrowingRecord);

        mentoringSession.setStatus("WAITING_ALLOCATION");
        mentoringSessionRepository.save(mentoringSession);
    }

    @Transactional
    public void confirmExport(Long borrowingRecordId) {
        BorrowingRecord borrowingRecord = borrowingRecordRepository.findById(borrowingRecordId).orElseThrow(() -> new IllegalArgumentException("Phiếu mượn không tồn tại"));

        if (!"WAITING_ALLOCATION".equals(borrowingRecord.getStatus())) {
            throw new IllegalArgumentException("Chỉ xuất kho phiếu đang chờ cấp phát");
        }

        for (BorrowingDetail detail : borrowingRecord.getDetails()) {
            Equipment equipment = detail.getEquipment();
            if (equipment.getQuantity() < detail.getQuantity()) {
                throw new IllegalArgumentException("Không đủ tồn kho cho thiết bị: " + equipment.getName());
            }
        }

        for (BorrowingDetail detail : borrowingRecord.getDetails()) {
            Equipment equipment = detail.getEquipment();
            equipment.setQuantity(equipment.getQuantity() - detail.getQuantity());
            equipmentRepository.save(equipment);
        }

        borrowingRecord.setStatus("EXPORTED");
        borrowingRecord.setExportedAt(new Date());
        borrowingRecordRepository.save(borrowingRecord);

        if (borrowingRecord.getSession() != null) {
            borrowingRecord.getSession().setStatus("EXPORTED");
            mentoringSessionRepository.save(borrowingRecord.getSession());
        }
    }

    public void updateStatus(Long id, String status) {
        MentoringSession session = getById(id);
        if (session != null) {
            session.setStatus(status);
            mentoringSessionRepository.save(session);
        }
    }

    @Transactional
    public void cancelByStudent(Long sessionId, User student) {
        if (student == null || !"STUDENT".equals(student.getRole())) {
            throw new IllegalArgumentException("Bạn cần đăng nhập bằng tài khoản sinh viên");
        }

        MentoringSession mentoringSession = getById(sessionId);
        if (mentoringSession == null) {
            throw new IllegalArgumentException("Lịch tư vấn không tồn tại");
        }

        if (mentoringSession.getStudent() == null ||
                !mentoringSession.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền hủy lịch này");
        }

        if (!"PENDING".equals(mentoringSession.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hủy lịch đang chờ xác nhận");
        }

        mentoringSession.setStatus("CANCELLED");
        mentoringSessionRepository.save(mentoringSession);
    }

    public List<Object[]> getBorrowedEquipmentStats() {
        return borrowingRecordRepository.getBorrowedEquipmentStats();
    }

    public List<Object[]> getTopLecturers() {
        return mentoringSessionRepository.getTopLecturers(PageRequest.of(0, 5));
    }

}
