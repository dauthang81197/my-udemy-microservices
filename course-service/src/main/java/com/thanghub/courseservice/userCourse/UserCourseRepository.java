package com.thanghub.courseservice.userCourse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, UUID>, JpaSpecificationExecutor<UserCourse> {
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    UserCourse findByUserId(UUID userId);

    List<UserCourse> findAllByUserId(UUID userId);

}