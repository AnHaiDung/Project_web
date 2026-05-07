package com.demo.service;

import com.demo.model.entity.User;
import com.demo.model.entity.UserProfile;
import com.demo.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void register(User user) {
        // 1. Mã hóa mật khẩu
        String hashPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashPassword);

        if (user.getRole() == null) {
            user.setRole("STUDENT");
        }


        if (user.getProfile() == null) {
            UserProfile profile = new UserProfile();
            profile.setFullName(user.getUsername());
            profile.setUser(user);
            user.setProfile(profile);
        } else {
            user.getProfile().setUser(user);
        }

        userRepository.save(user);
    }

    public User checkLogin(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public boolean isUsernameExist(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}