package com.thanghub.courseservice.media;

import com.thanghub.courseservice.media.response.VideoFileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VideoFileService {
    VideoFileResponse uploadVideo(String nameSection, MultipartFile file) throws IOException;
    List<VideoFileResponse> uploadVideos(String nameSection, List<MultipartFile> files) throws IOException;
    Page<VideoFileResponse> getVideoFiles(Pageable pageable);
    VideoFileResponse getVideoFile(String id);
    VideoFileResponse deleteVideoFile(String id);
}
