package com.thanghub.courseservice.media;

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

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrlBase;

    @Override
    public VideoFileResponse uploadVideo(String nameSection, MultipartFile file) throws IOException {
        return toResponse(videoFileRepository.save(buildAndUpload(nameSection, file)));
    }

    @Override
    public List<VideoFileResponse> uploadVideos(String nameSection, List<MultipartFile> files) throws IOException {
        List<VideoFileResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(toResponse(videoFileRepository.save(buildAndUpload(nameSection, file))));
        }
        return results;
    }

    private VideoFile buildAndUpload(String nameSection, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);
        String r2Key = "videos/" + UUID.randomUUID() + (ext != null ? "." + ext : "");

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
    public Page<VideoFileResponse> getVideoFiles(Pageable pageable) {
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
                .map(p -> CompletedPart.builder()
                        .partNumber(p.getPartNumber())
                        .eTag(p.getETag())
                        .build())
                .toList();

        r2Client.completeMultipartUpload(r -> r
                .bucket(bucket)
                .key(req.getKey())
                .uploadId(req.getUploadId())
                .multipartUpload(m -> m.parts(parts)));

        VideoFile videoFile = new VideoFile();
        videoFile.setNameSection(req.getNameSection());
        videoFile.setOriginalFilename(req.getOriginalFilename());
        videoFile.setContentType(req.getContentType());
        videoFile.setFileSize(req.getFileSize());
        videoFile.setR2Key(req.getKey());
        videoFile.setPublicUrl(publicUrlBase + "/" + req.getKey());
        videoFile.setStatus(VideoFileStatus.ACTIVE);
        return toResponse(videoFileRepository.save(videoFile));
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
