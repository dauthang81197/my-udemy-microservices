package com.thanghub.courseservice.media.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VideoFileResponse {
    private String id;
    private String nameSection;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String publicUrl;
    private String presignedUrl;
    private String status;
    private LocalDateTime createdAt;
}
