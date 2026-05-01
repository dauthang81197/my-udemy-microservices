package com.thanghub.courseservice.userLessonProgress;

import com.thanghub.common.BaseEntity;
import com.thanghub.courseservice.lesson.Lesson;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_lesson_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLessonProgress extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private int currentTime = 0;

    @Column(nullable = false)
    private boolean isCompleted = false;
}
