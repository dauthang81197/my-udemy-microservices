package com.thanghub.courseservice.media.request;

import lombok.Data;

import java.util.List;

@Data
public class CompleteUploadRequest {
    private String key;
    private String uploadId;
    private String nameSection;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private List<PartInfo> parts;

    @Data
    public static class PartInfo {
        private int partNumber;
        private String eTag;
    }
}