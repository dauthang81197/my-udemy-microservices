package com.thanghub.courseservice.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository()
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {
    Optional<Course> findByTitle(String title);

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.sections WHERE c.id = :id")
    Optional<Course> findByIdWithDetails(@Param("id") UUID id);
}
