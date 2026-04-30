package com.thanghub.courseservice.media;

import com.thanghub.courseservice.media.response.VideoFileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoFileService {
    VideoFileResponse uploadVideo(String name, MultipartFile file) throws IOException;
    Page<VideoFileResponse> getVideoFiles(Pageable pageable);
    VideoFileResponse getVideoFile(String id);
    VideoFileResponse deleteVideoFile(String id);
}
