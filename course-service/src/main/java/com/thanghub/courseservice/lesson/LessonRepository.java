package com.thanghub.courseservice.lesson;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    @EntityGraph(attributePaths = {"videoFile"})
    Page<Lesson> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"videoFile"})
    Optional<Lesson> findById(UUID id);
}
