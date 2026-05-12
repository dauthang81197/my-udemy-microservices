package com.thanghub.courseservice.userLessonProgress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UUID> {
    List<UserLessonProgress> findByUserIdAndLessonIdIn(UUID userId, List<UUID> lessonIds);
}
