package com.thanghub.courseservice.section;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    @Query("SELECT DISTINCT s FROM Section s LEFT JOIN FETCH s.lessons WHERE s.course.id = :courseId")
    List<Section> findByCourseIdWithLessons(@Param("courseId") UUID courseId);
}
