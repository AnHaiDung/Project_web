package com.demo.config;

import com.demo.model.entity.*;
import com.demo.repository.*;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private EquipmentRepository eqRepo;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LabRoomRepository labRoomRepository;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            // Khởi tạo Department
            Department itDept = null;
            if (departmentRepository.count() == 0) {
                itDept = new Department(null, "Công nghệ thông tin");
                departmentRepository.save(itDept);
                departmentRepository.save(new Department(null, "Kinh tế"));
                departmentRepository.save(new Department(null, "Ngôn ngữ Anh"));
            } else {
                itDept = departmentRepository.findAll().get(0);
            }

            // Khởi tạo LabRoom
            LabRoom lab402 = null;
            if (labRoomRepository.count() == 0) {
                lab402 = new LabRoom(null, "Phòng Lab 402");
                labRoomRepository.save(lab402);
                labRoomRepository.save(new LabRoom(null, "Phòng Lab 505"));
                labRoomRepository.save(new LabRoom(null, "Văn phòng khoa CNTT"));
            } else {
                lab402 = labRoomRepository.findAll().get(0);
            }

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(BCrypt.hashpw("admin123", BCrypt.gensalt()))
                        .role("ADMIN")
                        .build();

                UserProfile profile = UserProfile.builder()
                        .fullName("Quản trị viên hệ thống")
                        .user(admin)
                        .build();

                admin.setProfile(profile);
                userRepository.save(admin);
            }

            if (userRepository.findByUsername("gv01").isEmpty()) {
                User gv = new User();
                gv.setUsername("gv01");
                gv.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
                gv.setRole("LECTURER");

                UserProfile profile = UserProfile.builder()
                        .fullName("Giảng viên Nguyễn Văn A")
                        .user(gv)
                        .build();
                gv.setProfile(profile);

                Lecturer lecturer = new Lecturer();
                lecturer.setDepartment(itDept);
                lecturer.setLabRoom(lab402);
                lecturer.setUser(gv);
                gv.setLecturer(lecturer);

                userRepository.save(gv);
            }
        };
    }
}