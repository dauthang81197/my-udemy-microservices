package com.thanghub.courseservice.media;

import com.thanghub.courseservice.media.request.CompleteUploadRequest;
import com.thanghub.courseservice.media.response.VideoFileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface VideoFileService {
    VideoFileResponse uploadVideo(String courseId, String nameSection, MultipartFile file) throws IOException;
    List<VideoFileResponse> uploadVideos(String courseId, String nameSection, List<MultipartFile> files) throws IOException;
    Page<VideoFileResponse> getVideoFiles(String courseId, Pageable pageable);
    VideoFileResponse getVideoFile(String id);
    VideoFileResponse deleteVideoFile(String id);

    Map<String, String> initiateMultipartUpload(String filename);
    Map<String, String> presignUploadPart(String key, String uploadId, int partNumber);
    VideoFileResponse completeMultipartUpload(CompleteUploadRequest request);
}
