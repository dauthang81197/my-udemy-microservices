package com.thanghub.courseservice.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoFileRepository extends JpaRepository<VideoFile, UUID> {
}
