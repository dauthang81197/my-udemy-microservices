---
project_name: 'microservices'
user_name: 'Thang'
date: '2026-05-30'
sections_completed: ['technology_stack', 'critical_implementation_rules', 'development_workflow']
status: 'complete'
rule_count: 35
optimized_for_llm: true
---

# Project Context cho AI Agents

_File này chứa các quy tắc và pattern quan trọng mà AI agents phải tuân theo khi implement code trong dự án này. Tập trung vào các chi tiết không hiển nhiên mà agents có thể bỏ qua._

---

## Technology Stack & Versions

| Thành phần | Phiên bản | Service sử dụng & Ghi chú quan trọng |
|---|---|---|
| Java | 17 | Tất cả services. Temurin distribution trong CI. |
| Spring Boot | 3.4.5 | Tất cả services. |
| Spring Cloud Gateway | 2024.0.1 | gateway-service only. **Reactive/WebFlux — KHÔNG phải Servlet.** |
| JJWT | 0.11.5 | auth-service + gateway-service. **Bắt buộc đủ 3 artifact**: jjwt-api (compile), jjwt-impl (runtime), jjwt-jackson (runtime). |
| Springdoc OpenAPI | 2.8.3 (webmvc) / 2.6.0 (webflux) | **CRITICAL**: `webmvc-ui` cho auth/course, `webflux-ui` cho gateway. Nhầm artifact → Swagger UI broken, không có lỗi compiler. |
| AWS SDK v2 | 2.29.52 | course-service only. Cloudflare R2 qua S3-compatible API. Package: `software.amazon.awssdk.*` — KHÔNG phải `com.amazonaws.*`. |
| Apache POI | 5.3.0 | course-service only. Dùng `poi-ooxml`, không phải `poi` đơn thuần. |
| PostgreSQL / Liquibase / Redis / Lombok | BOM từ spring-boot-starter-parent | Xem pom.xml gốc cho version cụ thể. |

### Ràng buộc hệ thống không thể bỏ qua

- **JWT secret & algorithm phải CHUNG**: `jwt.secret` phải giống nhau ở cả 3 services. Thuật toán **HS256 (HMAC-SHA256)** — nếu một service dùng RS256, toàn hệ thống auth fail silently, không có error message rõ ràng.
- **Build order — flag `-am` bắt buộc**: `./mvnw clean install -pl {service} -am`. Thiếu `-am` → service dùng phiên bản cũ của common-lib từ `.m2` cache, lỗi chỉ xuất hiện khi test API.
- **Liquibase là bắt buộc, phải chạy migration trước**: `ddl-auto=validate` — schema KHÔNG tự tạo. Mọi thay đổi schema phải qua Liquibase changeset tại `src/main/resources/db/changelog/db.changelog-master.xml`. Deploy service trước khi DB được migrate → "Table not found".
- **Database isolation**: auth-service và course-service có schema DB **riêng biệt**, không share tables.
- **R2 endpoint override bắt buộc**: `S3Client` phải configure `endpointOverride()` trỏ đến `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`. Thiếu → request âm thầm gửi đến AWS thật.
- **JVM timezone**: `-Duser.timezone=Asia/Ho_Chi_Minh` set trong maven plugin. Phải set lại trong Dockerfile/startup script cho production.
- **Deployment topology**: Docker Swarm — 2 stacks: `my-udemy-backend-staging` (branch develop) và `my-udemy-backend` (branch main).

---

## Critical Implementation Rules

### Stack Boundary — Xác định trước khi viết bất kỳ dòng code nào

| Service | Stack | Test Client | Security Config |
|---|---|---|---|
| auth-service | Servlet (WebMVC) | `MockMvc` | `HttpSecurity` + custom `JwtAuthenticationFilter` |
| course-service | Servlet (WebMVC) | `MockMvc` | `HttpSecurity` + `oauth2-resource-server` |
| gateway-service | Reactive (WebFlux) | `WebTestClient` | `ServerHttpSecurity` + `SecurityWebFilterChain` |

`HttpServletRequest`, `OncePerRequestFilter`, `@EnableWebSecurity` (servlet-style) KHÔNG tồn tại trong gateway. Gateway dùng `ServerWebExchange`, `GlobalFilter`.

---

### Package & Naming Conventions

```
com.thanghub.{servicename}.{domain}.*
Ví dụ: com.thanghub.courseservice.course.*

Sub-packages trong domain:
  {domain}/request/     # *RequestDto
  {domain}/response/    # *Response hoặc *ResponseDto
  {domain}/             # Entity, Repository, Service interface, ServiceImpl, Controller
```

- **Service pattern**: interface `CourseService` + implementation `CourseServiceImpl`
- **Controller split**: `CourseController` (user-facing) + `CourseAdminController` (admin)
- **Column names**: snake_case trong DB (`full_name`, `is_preview`), camelCase trong Java field

---

### Entity Rules

```java
// Luôn extends BaseEntity — cung cấp: UUID id, createdAt, updatedAt, deletedAt
@Entity
@Table(name = "courses")
@Where(clause = "deleted_at IS NULL")  // auto-filter soft-deleted records
public class Course extends BaseEntity { ... }

// Lombok trên Entity: KHÔNG dùng @Data (gây vấn đề lazy loading)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
```

- UUID cho tất cả IDs. Parse từ String: `UUID.fromString(id)` — ném `IllegalArgumentException` nếu sai format
- Liquibase column type: `uuid` (PostgreSQL native), KHÔNG dùng `varchar(36)`
- KHÔNG tự set `createdAt`/`updatedAt` trong service layer
- join table (many-to-many): KHÔNG cần extends BaseEntity

---

### Soft Delete

```java
// ĐÚNG
entity.setDeletedAt(LocalDateTime.now());
repository.save(entity);

// SAI — xóa vật lý, vi phạm soft-delete pattern
repository.delete(entity);
```

`@Where(clause = "deleted_at IS NULL")` trên entity → Hibernate tự filter. Nếu không có, mọi repository method phải thêm `AndDeletedAtIsNull`.

---

### Transaction Boundaries

```java
// Service layer — KHÔNG phải Controller, KHÔNG phải Repository
@Transactional(readOnly = true)   // mọi read operation
public CourseResponse getCourse(UUID id) { ... }

@Transactional                     // mọi write operation
public CourseResponse createCourse(CreateCourseRequestDto dto) { ... }
```

- `@Transactional` trên public method gọi private method trong cùng class → Spring AOP bỏ qua (self-invocation)
- Lazy-load collection bên ngoài transaction → `LazyInitializationException`

---

### Exception Handling

- Service layer **throw exception**, KHÔNG return null
- Custom exceptions từ `common-lib/exception/` (ví dụ: `MyBadRequestException`)
- Mỗi service có `@RestControllerAdvice` catch exception, map về `ApiResponseBase.error(...)` hoặc `ApiResponseBase.fail(...)`
- Không để exception bubble up thành 500 khi đơn giản là 404

---

### API Response & Pagination

```java
// Luôn wrap response
return ResponseEntity.ok(ApiResponseBase.ok("Message", data));

// Pagination — trả về PaginationResponse, không phải Page<T>
return PaginationMapper.from(page);  // từ common-lib
```

---

### Bean Validation

```java
// DTO: khai báo constraints
public class CreateCourseRequestDto {
    @NotBlank @Size(max = 255) private String title;
}

// Controller: @Valid bắt buộc
public ResponseEntity<?> createCourse(@Valid @RequestBody CreateCourseRequestDto dto) { ... }

// Service: assume data đã clean — KHÔNG validate lại
```

---

### Dynamic Queries — Specification Pattern

```java
// Repository implements JpaSpecificationExecutor<T>
public interface CourseRepository extends JpaRepository<Course, UUID>,
    JpaSpecificationExecutor<Course> {}

// Combine specifications
Specification<Course> spec = Specification
    .where(CourseSpecification.hasStatus(status))
    .and(CourseSpecification.titleContains(title));
courseRepository.findAll(spec, pageable);
```

KHÔNG dùng dynamic JPQL string concatenation.

---

### JWT Claims (course-service)

```java
// oauth2-resource-server đã parse JWT sẵn
@AuthenticationPrincipal Jwt jwt
UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

// JWT: subject = email, "roles" claim (prefix "ROLE_"), "permissions" claim (no prefix)
// getClaimAsString() trả về null nếu claim không tồn tại — phải null-check trước UUID.fromString()
```

---

### Security Filter (auth-service)

```java
// PHẢI extends OncePerRequestFilter để tránh double-execution
public class JwtAuthenticationFilter extends OncePerRequestFilter { ... }

// Public endpoints: whitelist trong SecurityConfig.filterChain()
// KHÔNG dùng @PermitAll trên controller
.requestMatchers("/login", "/register", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

---

### Redis

```java
// Key format: {entity_type}:{identifier}
String key = "refresh_token:" + userId;

// LUÔN có TTL — không lưu không expiry
redisTemplate.opsForValue().set(key, token, duration, TimeUnit.MILLISECONDS);
// Dùng StringRedisTemplate cho string values
```

---

### Cloudflare R2 — File Upload (course-service only)

```java
// Lưu objectKey, KHÔNG lưu full URL vào DB
videoFile.setR2Key("/video/" + UUID.randomUUID() + ".mp4");
// Generate URL khi cần: publicUrlBase + "/" + r2Key

// Validate file type/size ở Controller trước khi gọi Service
// Upload failure → rollback DB transaction (@Transactional)
// S3Client phải configure endpointOverride() + forcePathStyle(true) cho R2
```

---

### Environment Variables

```properties
# Luôn dùng ${ENV_VAR:default_value} — KHÔNG hardcode production values
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/mydb}
```

---

### Swagger / OpenAPI

```java
@Tag(name = "Course")                 // class level
@SecurityRequirements                 // endpoint public — no auth
@Operation(summary = "...", description = "...")
@ApiResponses({@ApiResponse(responseCode = "200", description = "...")})
```

---

### Testing Rules

**Test properties bắt buộc** (`src/test/resources/application.properties`):
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
spring.liquibase.enabled=false
```

**Chọn test annotation đúng**:
- Unit test Service: `@ExtendWith(MockitoExtension.class)` — không load Spring context
- Controller test: `@WebMvcTest(XxxController.class)` + `@MockBean` cho dependencies
- Integration test: `@SpringBootTest` + H2 hoặc Testcontainers

**Mock bắt buộc**:
```java
@MockBean S3Client s3Client;           // mọi test có upload logic (course-service)
@MockBean RedisTemplate redisTemplate; // unit tests có Redis dependency
```

**JWT trong test (WebMVC)**:
```java
mockMvc.perform(get("/api/courses")
    .with(jwt().jwt(j -> j.claim("userId", userId.toString()))));
```

**Assertions qua ApiResponseBase wrapper**:
```java
.andExpect(jsonPath("$.success").value(true))
.andExpect(jsonPath("$.data.id").value(courseId.toString()))
// KHÔNG: jsonPath("$.id") — data luôn nằm trong $.data.*
```

**Test naming**: `methodName_stateUnderTest_expectedBehavior`

---

## Development Workflow Rules

### Git & Branch Strategy

- **Main branch cho PR**: `develop` (không phải `main`)
- **Branch naming**: `{type}/{description}` — ví dụ: `cicd/github-action`, `feat/course-filter`
- **CI trigger**: mọi push trên tất cả branches (`branches: ["**"]`)
- **Deploy trigger**: chỉ `develop` (→ staging) và `main` (→ production)

### CI/CD Pipeline

```bash
# CI: build + test mỗi service song song (matrix strategy)
./mvnw clean verify -pl {service} -am

# Deploy: build JAR, Docker image, push, deploy Docker Swarm
./mvnw clean package -pl {service} -am -Dmaven.test.skip=true
```

- **Staging**: branch `develop` → Docker tag `staging` → stack `my-udemy-backend-staging`
- **Production**: branch `main` → Docker tag `latest` + `{7-char SHA}` → stack `my-udemy-backend`
- Docker image name: `{DOCKER_USERNAME}/{service-name}:{tag}`

### Liquibase Changeset — Quy tắc bất khả xâm phạm

```xml
<!-- File: src/main/resources/db/changelog/changes/NN-description.xml -->
<!-- Đặt tên file: số thứ tự 2 chữ số + mô tả ngắn -->
<!-- Ví dụ: 11-add-video-duration-column.xml -->

<changeSet id="unique-id" author="thang">
    <!-- id phải unique trong toàn bộ project -->
    <!-- KHÔNG BAO GIỜ sửa changeset đã chạy — chỉ thêm changeset mới -->
</changeSet>
```

- Khai báo file mới trong `db.changelog-master.xml` bằng `<include file="..."/>` — append vào cuối
- Thứ tự `<include>` là thứ tự thực thi

### Thêm tính năng mới — Checklist

1. Xác định service phù hợp (auth / course / gateway)
2. Xác định stack (Servlet hay Reactive)
3. Nếu thêm entity/column → tạo Liquibase changeset **trước** khi viết code
4. Build với `-am` để đảm bảo common-lib up-to-date
5. Test properties override DB/Redis/S3
6. PR target: branch `develop`

### Local Development

```bash
# Build tất cả
./mvnw clean install -DskipTests

# Build một service (với dependencies)
./mvnw clean install -pl course-service -am -DskipTests

# Run một service
cd auth-service && ../mvnw spring-boot:run

# Test một service
./mvnw test -pl auth-service
```

Swagger UI aggregated: `http://localhost:8000/swagger-ui.html`

---

## Usage Guidelines

**Dành cho AI Agents:**
- Đọc file này trước khi implement bất kỳ code nào trong dự án
- Tuân theo TẤT CẢ rules chính xác như đã ghi
- Khi nghi ngờ, chọn option có ràng buộc chặt hơn
- Ưu tiên: Stack Boundary → Transaction → Exception Handling → Naming

**Dành cho Developers:**
- Cập nhật file này khi tech stack thay đổi
- Review mỗi quý để loại bỏ rules lỗi thời
- Giữ nội dung lean — chỉ thêm rules thực sự non-obvious

_Last Updated: 2026-05-30_
