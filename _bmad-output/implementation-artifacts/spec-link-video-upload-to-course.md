---
title: 'Link video upload to course (mandatory courseId)'
type: 'feature'
created: '2026-06-06'
status: 'done'
baseline_commit: '584578a432c1dc23c42ca06a42a27e86396100b1'
context:
  - '{project-root}/_bmad-output/project-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Video files được upload độc lập, không gắn với khóa học nào — khiến admin phải tự nhớ và cung cấp `videoIds` thủ công khi gọi auto-setup, dễ nhầm lẫn.

**Approach:** Thêm `courseId` bắt buộc vào tất cả các upload endpoint (single, multiple, multipart complete). Lưu FK `course_id` vào bảng `video_files`. Thêm filter `courseId` vào `GET /admin/videos` để UI có thể lọc video theo khóa học khi chuẩn bị gọi auto-setup. Auto-setup giữ nguyên (option B — vẫn nhận `videoIds` trong body).

## Boundaries & Constraints

**Always:**
- `nameSection` giữ nguyên — không thay đổi hoặc xóa.
- Column `course_id` thêm vào DB là nullable (tương thích với rows cũ). API enforce bắt buộc ở tầng controller.
- `VideoFile` dùng `@ManyToOne(fetch = FetchType.LAZY)` tới `Course`.
- `initiateMultipartUpload` không tạo VideoFile → không cần thêm `courseId`.
- Endpoint auto-setup (`POST /admin/courses/{id}/auto-setup`) không thay đổi.

**Ask First:**
- Nếu cần thêm validation kiểm tra `courseId` có tồn tại trong DB không (thay vì để FK constraint bắt) — hỏi trước.

**Never:**
- Không xóa field `nameSection` khỏi entity, request, response.
- Không thay đổi logic tạo Section/Lesson trong auto-setup.
- Không migrate dữ liệu cũ (set courseId cho rows đã có) — ngoài scope.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Upload single với courseId hợp lệ | `POST /admin/videos/upload` + `courseId`, `nameSection`, `file` | VideoFileResponse có `courseId` | — |
| Upload thiếu `courseId` | `POST /admin/videos/upload` không có `courseId` param | 400 Bad Request | Spring MVC MissingServletRequestParameterException |
| Complete multipart với courseId | `CompleteUploadRequest` có `courseId` | VideoFile được tạo với `course_id` | — |
| List videos filter theo courseId | `GET /admin/videos?courseId={uuid}` | Chỉ trả videos thuộc course đó | — |
| List videos không có filter | `GET /admin/videos` | Trả toàn bộ videos (hành vi cũ) | — |

</frozen-after-approval>

## Code Map

- `course-service/src/main/resources/db/changelog/changes/10-alter-video-files-add-course-id.xml` — Liquibase changeset thêm cột `course_id uuid` nullable
- `course-service/src/main/resources/db/changelog/db.changelog-master.xml` — include changeset 10
- `course-service/src/main/java/com/thanghub/courseservice/media/VideoFile.java` — thêm `@ManyToOne Course course`
- `course-service/src/main/java/com/thanghub/courseservice/media/VideoFileAdminController.java` — thêm `courseId` param vào upload endpoints; thêm `courseId` filter param vào list
- `course-service/src/main/java/com/thanghub/courseservice/media/VideoFileService.java` — cập nhật signatures
- `course-service/src/main/java/com/thanghub/courseservice/media/VideoFileServiceImpl.java` — cập nhật impl; thêm filter logic
- `course-service/src/main/java/com/thanghub/courseservice/media/VideoFileRepository.java` — thêm query filter theo courseId
- `course-service/src/main/java/com/thanghub/courseservice/media/request/CompleteUploadRequest.java` — thêm `courseId`
- `course-service/src/main/java/com/thanghub/courseservice/media/response/VideoFileResponse.java` — thêm `courseId`

## Tasks & Acceptance

**Execution:**
- [x] `db/changelog/changes/10-alter-video-files-add-course-id.xml` -- tạo file mới, thêm `addColumn` với `course_id uuid` nullable, FK tới bảng `courses` -- Liquibase migration trước khi thay đổi code
- [x] `db/changelog/db.changelog-master.xml` -- append `<include file="/db/changelog/changes/10-alter-video-files-add-course-id.xml"/>` -- khai báo changeset mới
- [x] `media/VideoFile.java` -- thêm `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_id") Course course` -- liên kết entity
- [x] `media/response/VideoFileResponse.java` -- thêm field `String courseId` -- expose courseId trong response
- [x] `media/request/CompleteUploadRequest.java` -- thêm field `String courseId` -- nhận courseId khi complete multipart
- [x] `media/VideoFileRepository.java` -- thêm `Page<VideoFile> findByCourse_Id(UUID courseId, Pageable pageable)` -- filter theo course
- [x] `media/VideoFileService.java` -- cập nhật `uploadVideo`, `uploadVideos` thêm param `String courseId`; cập nhật `getVideoFiles` thêm param `String courseId` (nullable) -- interface contract
- [x] `media/VideoFileServiceImpl.java` -- cập nhật `buildAndUpload` nhận `courseId`, load Course entity và set vào VideoFile; cập nhật `getVideoFiles` dùng repository filter khi courseId có giá trị; cập nhật `completeMultipartUpload` dùng `req.getCourseId()` -- logic thực thi
- [x] `media/VideoFileAdminController.java` -- thêm `@RequestParam String courseId` vào `/upload` và `/upload-multiple`; thêm `@RequestParam(required = false) String courseId` vào `GET /` -- điểm vào API
- [x] `media/VideoFileController.java` -- thêm `@RequestParam String courseId` vào `/upload`; thêm `@RequestParam(required = false) String courseId` vào `GET /` -- user-facing controller cùng dùng interface

**Acceptance Criteria:**
- Given upload single video, when `courseId` không được truyền, then Spring trả 400 tự động.
- Given upload single video với `courseId` hợp lệ, when gọi API, then `VideoFileResponse` chứa `courseId` đúng.
- Given upload multiple videos với `courseId`, when gọi API, then tất cả videos trong response có cùng `courseId`.
- Given complete multipart upload với `courseId` trong body, when gọi API, then VideoFile được tạo với `course_id` đúng trong DB.
- Given `GET /admin/videos?courseId={uuid}`, when gọi API, then chỉ trả videos thuộc course đó.
- Given `GET /admin/videos` không có `courseId`, when gọi API, then trả toàn bộ videos (backward compatible).

## Design Notes

`getVideoFiles` dùng conditional query — nếu `courseId != null` dùng `findByCourseId`, ngược lại dùng `findAll`:

```java
public Page<VideoFileResponse> getVideoFiles(String courseId, Pageable pageable) {
    Page<VideoFile> page = courseId != null
        ? videoFileRepository.findByCourseId(UUID.fromString(courseId), pageable)
        : videoFileRepository.findAll(pageable);
    return page.map(this::toResponse);
}
```

`toResponse` map `courseId` từ `videoFile.getCourse()` — null-safe vì rows cũ không có course:

```java
.courseId(videoFile.getCourse() != null ? videoFile.getCourse().getId().toString() : null)
```

## Verification

**Commands:**
- `./mvnw clean install -pl course-service -am -DskipTests` -- expected: BUILD SUCCESS
- `./mvnw test -pl course-service` -- expected: BUILD SUCCESS, no test failures

## Suggested Review Order

**Schema & Migration**

- Changeset thêm `course_id uuid nullable` với FK tới `courses(id)`
  [`10-alter-video-files-add-course-id.xml:8`](../../course-service/src/main/resources/db/changelog/changes/10-alter-video-files-add-course-id.xml#L8)

- Include changeset mới vào master changelog
  [`db.changelog-master.xml:17`](../../course-service/src/main/resources/db/changelog/db.changelog-master.xml#L17)

**Entity & Data Model**

- ManyToOne LAZY relationship — nullable, FK `course_id`
  [`VideoFile.java:41`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFile.java#L41)

- courseId exposed trong response, null-safe cho rows cũ
  [`VideoFileResponse.java:12`](../../course-service/src/main/java/com/thanghub/courseservice/media/response/VideoFileResponse.java#L12)

- courseId field trong multipart complete request body
  [`CompleteUploadRequest.java:12`](../../course-service/src/main/java/com/thanghub/courseservice/media/request/CompleteUploadRequest.java#L12)

**Core Upload Logic**

- Entry point: `findCourse` trước khi upload; binding Course vào VideoFile
  [`VideoFileServiceImpl.java:50`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFileServiceImpl.java#L50)

- getVideoFiles: conditional filter khi courseId có giá trị
  [`VideoFileServiceImpl.java:93`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFileServiceImpl.java#L93)

- parseCourseId + findCourse: 400 cho UUID sai format, 404 cho course không tồn tại
  [`VideoFileServiceImpl.java:184`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFileServiceImpl.java#L184)

- Spring Data derived query traversal qua ManyToOne relationship
  [`VideoFileRepository.java:12`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFileRepository.java#L12)

**API Surface**

- Admin upload single/multiple: courseId bắt buộc
  [`VideoFileAdminController.java:40`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFileAdminController.java#L40)

- Admin list: courseId optional filter cho UI
  [`VideoFileAdminController.java:72`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFileAdminController.java#L72)

- User-facing upload: courseId bắt buộc (same pattern)
  [`VideoFileController.java:38`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFileController.java#L38)

**Interface Contract**

- Updated service interface signatures
  [`VideoFileService.java:14`](../../course-service/src/main/java/com/thanghub/courseservice/media/VideoFileService.java#L14)
