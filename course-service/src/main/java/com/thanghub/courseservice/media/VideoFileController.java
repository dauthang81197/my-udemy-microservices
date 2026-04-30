package com.thanghub.courseservice.media;

import com.thanghub.common.ApiResponseBase;
import com.thanghub.common.mapper.PaginationMapper;
import com.thanghub.common.response.PaginationResponse;
import com.thanghub.courseservice.media.response.VideoFileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("videos")
@RequiredArgsConstructor
@Tag(name = "Video")
public class VideoFileController {

    private final VideoFileService videoFileService;

    @Operation(summary = "Upload video", description = "Upload video file to Cloudflare R2")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload successfully"),
            @ApiResponse(responseCode = "400", description = "File is empty or invalid")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseBase.fail("File không được để trống"));
        }
        VideoFileResponse response = videoFileService.uploadVideo(name, file);
        return ResponseEntity.ok(ApiResponseBase.ok("Upload successfully", response));
    }

    @Operation(summary = "List videos", description = "Return paginated video file list")
    @GetMapping
    public PaginationResponse<VideoFileResponse> getVideoFiles(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<VideoFileResponse> page = videoFileService.getVideoFiles(pageable);
        return PaginationMapper.from(page);
    }

    @Operation(summary = "Get video", description = "Return video file detail")
    @GetMapping("/{id}")
    public ResponseEntity<?> getVideoFile(@PathVariable String id) {
        VideoFileResponse response = videoFileService.getVideoFile(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Success", response));
    }

    @Operation(summary = "Delete video", description = "Delete video from R2 and mark as deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVideoFile(@PathVariable String id) {
        VideoFileResponse response = videoFileService.deleteVideoFile(id);
        return ResponseEntity.ok(ApiResponseBase.ok("Deleted successfully", response));
    }
}
