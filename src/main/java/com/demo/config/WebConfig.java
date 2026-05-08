package com.demo.config;

import com.demo.model.entity.Department;
import com.demo.model.entity.LabRoom;
import com.demo.model.entity.Lecturer;
import com.demo.model.entity.User;
import com.demo.model.entity.UserProfile;
import com.demo.repository.DepartmentRepository;
import com.demo.repository.LabRoomRepository;
import com.demo.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LabRoomRepository labRoomRepository;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            Department cntt = seedDepartment("Công nghệ thông tin");
            Department kinhTe = seedDepartment("Kinh tế");
            Department ngonNguAnh = seedDepartment("Ngôn ngữ Anh");
            seedDepartment("Điện tử viễn thông");

            seedLabRoom("Phòng Lab 402");
            seedLabRoom("Phòng Lab 505");
            seedLabRoom("Văn phòng khoa CNTT");
            seedLabRoom("Phòng thực hành mạng");

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

            seedLecturer(userRepository, "gv01", "123456", "Giảng viên Nguyễn Văn A", cntt);
            seedLecturer(userRepository, "gv02", "123456", "Giảng viên Trần Thị B", kinhTe);
            seedLecturer(userRepository, "gv03", "123456", "Giảng viên Lê Văn C", ngonNguAnh);
        };
    }

    private Department seedDepartment(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> departmentRepository.save(new Department(null, name)));
    }

    private void seedLabRoom(String roomName) {
        boolean exists = labRoomRepository.findAll()
                .stream()
                .anyMatch(room -> roomName.equals(room.getRoomName()));

        if (!exists) {
            labRoomRepository.save(new LabRoom(null, roomName));
        }
    }

    private void seedLecturer(UserRepository userRepository,
                              String username,
                              String password,
                              String fullName,
                              Department department) {
        User user = userRepository.findByUsername(username).orElseGet(User::new);

        if (user.getId() == null) {
            user.setUsername(username);
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }

        user.setRole("LECTURER");

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }
        profile.setFullName(fullName);
        user.setProfile(profile);

        Lecturer lecturer = user.getLecturer();
        if (lecturer == null) {
            lecturer = new Lecturer();
            lecturer.setUser(user);
        }
        lecturer.setUser(user);
        lecturer.setDepartment(department);
        user.setLecturer(lecturer);

        userRepository.save(user);
    }
}
