package com.thanghub.courseservice.userCourse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, UUID> {
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);
}