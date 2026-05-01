package com.thanghub.courseservice.userCourse;

import com.thanghub.common.BaseEntity;
import com.thanghub.courseservice.course.Course;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCourse extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserCourseStatusEnum status = UserCourseStatusEnum.ENROLLED;

    @Column(nullable = false)
    private int progress = 0;
}