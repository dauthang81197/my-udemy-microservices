package com.thanghub.courseservice.media;

import com.thanghub.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "video_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoFile extends BaseEntity {

    @Column(name = "name_section", nullable = false)
    private String nameSection;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "r2_key", nullable = false, unique = true, length = 500)
    private String r2Key;

    @Column(name = "public_url", nullable = false, length = 2000)
    private String publicUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VideoFileStatus status = VideoFileStatus.ACTIVE;
}
