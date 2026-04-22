package com.thanghub.courseservice.course;

import com.thanghub.courseservice.common.BaseEntity;
import com.thanghub.courseservice.common.enums.CourseStatusEnum;
import com.thanghub.courseservice.common.enums.LevelEnum;
import com.thanghub.courseservice.section.Section;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelEnum level = LevelEnum.BEGINNER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatusEnum status = CourseStatusEnum.DRAFT;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();
}