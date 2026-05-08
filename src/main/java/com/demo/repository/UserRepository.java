package com.demo.repository;

import com.demo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByRole(String role);

    @Query("select u from User u where u.role = 'LECTURER' and u.lecturer.department.id = :departmentId")
    List<User> findLecturersByDepartmentId(@Param("departmentId") Long departmentId);
}
