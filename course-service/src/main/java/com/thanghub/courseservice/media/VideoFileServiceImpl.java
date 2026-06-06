package com.thanghub.courseservice.media;

import com.thanghub.courseservice.course.Course;
import com.thanghub.courseservice.course.CourseRepository;
import com.thanghub.common.exception.MyBadRequestException;
import com.thanghub.courseservice.media.request.CompleteUploadRequest;
import com.thanghub.courseservice.media.response.VideoFileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoFileServiceImpl implements VideoFileService {

    private final S3Client r2Client;
    private final S3Presigner r2Presigner;
    private final VideoFileRepository videoFileRepository;
    private final CourseRepository courseRepository;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrlBase;

    @Override
    public VideoFileResponse uploadVideo(String courseId, String nameSection, MultipartFile file) throws IOException {
        Course course = findCourse(courseId);
        return toResponse(videoFileRepository.save(buildAndUpload(course, nameSection, file)));
    }

    @Override
    public List<VideoFileResponse> uploadVideos(String courseId, String nameSection, List<MultipartFile> files) throws IOException {
        Course course = findCourse(courseId);
        List<VideoFileResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(toResponse(videoFileRepository.save(buildAndUpload(course, nameSection, file))));
        }
        return results;
    }

    private VideoFile buildAndUpload(Course course, String nameSection, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);
        String r2Key = "/video" + UUID.randomUUID() + (ext != null ? "." + ext : "");

        r2Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(r2Key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        VideoFile videoFile = new VideoFile();
        videoFile.setCourse(course);
        videoFile.setNameSection(nameSection);
        videoFile.setOriginalFilename(originalFilename);
        videoFile.setContentType(file.getContentType());
        videoFile.setFileSize(file.getSize());
        videoFile.setR2Key(r2Key);
        videoFile.setPublicUrl(publicUrlBase + "/" + r2Key);
        videoFile.setStatus(VideoFileStatus.ACTIVE);
        return videoFile;
    }

    @Override
    public Page<VideoFileResponse> getVideoFiles(String courseId, Pageable pageable) {
        if (courseId != null) {
            return videoFileRepository.findByCourse_Id(parseCourseId(courseId), pageable).map(this::toResponse);
        }
        return videoFileRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public VideoFileResponse getVideoFile(String id) {
        VideoFile videoFile = findById(id);
        String presignedUrl = generatePresignedUrl(videoFile.getR2Key());
        return toResponse(videoFile, presignedUrl);
    }

    @Override
    public VideoFileResponse deleteVideoFile(String id) {
        VideoFile videoFile = findById(id);
        r2Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(videoFile.getR2Key())
                .build());
        videoFile.setStatus(VideoFileStatus.DELETED);
        return toResponse(videoFileRepository.save(videoFile));
    }

    @Override
    public Map<String, String> initiateMultipartUpload(String filename) {
        var response = r2Client.createMultipartUpload(b -> b
                .bucket(bucket)
                .key(filename)
                .contentType("video/mp4"));
        return Map.of("uploadId", response.uploadId(), "key", filename);
    }

    @Override
    public Map<String, String> presignUploadPart(String key, String uploadId, int partNumber) {
        var presignedUrl = r2Presigner.presignUploadPart(r -> r
                .signatureDuration(Duration.ofMinutes(60))
                .uploadPartRequest(u -> u
                        .bucket(bucket)
                        .key(key)
                        .uploadId(uploadId)
                        .partNumber(partNumber)));
        return Map.of("url", presignedUrl.url().toString());
    }

    @Override
    public VideoFileResponse completeMultipartUpload(CompleteUploadRequest req) {
        var parts = req.getParts().stream()
                .map(p -> {
                    String eTag = p.getETag();
                    if (eTag == null || eTag.isBlank()) {
                        throw new IllegalArgumentException(
                                "ETag is null for part " + p.getPartNumber() +
                                        ". Ensure CORS ExposeHeaders includes ETag on the R2 bucket.");
                    }
                    // R2/S3 requires ETag wrapped in double quotes in the CompleteMultipartUpload XML
                    if (!eTag.startsWith("\"")) {
                        eTag = "\"" + eTag + "\"";
                    }
                    return CompletedPart.builder()
                            .partNumber(p.getPartNumber())
                            .eTag(eTag)
                            .build();
                })
                .toList();

        r2Client.completeMultipartUpload(r -> r
                .bucket(bucket)
                .key(req.getKey())
                .uploadId(req.getUploadId())
                .multipartUpload(m -> m.parts(parts)));

        Course course = findCourse(req.getCourseId());
        VideoFile videoFile = new VideoFile();
        videoFile.setCourse(course);
        videoFile.setNameSection(req.getNameSection());
        videoFile.setOriginalFilename(req.getOriginalFilename());
        videoFile.setContentType(req.getContentType());
        videoFile.setFileSize(req.getFileSize());
        videoFile.setR2Key(req.getKey());
        videoFile.setPublicUrl(publicUrlBase + "/" + req.getKey());
        videoFile.setStatus(VideoFileStatus.ACTIVE);
        return toResponse(videoFileRepository.save(videoFile));
    }

    private Course findCourse(String courseId) {
        return courseRepository.findById(parseCourseId(courseId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found: " + courseId));
    }

    private UUID parseCourseId(String courseId) {
        try {
            return UUID.fromString(courseId);
        } catch (IllegalArgumentException e) {
            throw new MyBadRequestException("Invalid courseId format: " + courseId);
        }
    }

    private VideoFile findById(String id) {
        return videoFileRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Video file not found: " + id));
    }

    private String generatePresignedUrl(String r2Key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(r2Key)
                        .build())
                .build();
        return r2Presigner.presignGetObject(presignRequest).url().toString();
    }

    private VideoFileResponse toResponse(VideoFile videoFile) {
        return toResponse(videoFile, null);
    }

    private VideoFileResponse toResponse(VideoFile videoFile, String presignedUrl) {
        return VideoFileResponse.builder()
                .id(videoFile.getId().toString())
                .courseId(videoFile.getCourse() != null ? videoFile.getCourse().getId().toString() : null)
                .nameSection(videoFile.getNameSection())
                .originalFilename(videoFile.getOriginalFilename())
                .contentType(videoFile.getContentType())
                .fileSize(videoFile.getFileSize())
                .publicUrl(videoFile.getPublicUrl())
                .presignedUrl(presignedUrl)
                .status(videoFile.getStatus().name())
                .createdAt(videoFile.getCreatedAt())
                .build();
    }
}
