package com.thanghub.courseservice.media.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CompleteUploadRequest {
    private String key;
    private String uploadId;
    private String courseId;
    private String nameSection;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private List<PartInfo> parts;

    @Data
    public static class PartInfo {
        private int partNumber;
        @JsonProperty("eTag")
        private String eTag;
    }
}