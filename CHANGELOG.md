# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased] — feat/course-service/media

### Added
- `userCourse` module: entity `UserCourse`, `UserCourseStatusEnum`, repository, service interface & impl, admin/user controllers, request/response DTOs
- Media/video upload endpoint integrated with Cloudflare R2
- Lesson refactor to support video media attachment

---

## [0.5.0] — feat/course-service/course

### Added
- Section module: CRUD API, Excel import (`SectionImportService`), import result DTO
- Course module: CRUD API with title, description, level, status fields
- `UpdateSectionRequestDto`, `CreateSectionRequestDto`

---

## [0.4.0] — feat/all-service/rbac

### Added
- Role-Based Access Control (RBAC) across all services
- Common library (`lib common`) updates for security integration
- Security configuration updates

---

## [0.3.0] — feat/gateway-service/init-base

### Added
- Gateway service base setup and routing configuration

---

## [0.2.0] — feat/course-service/create-tables

### Added
- Course service database schema and initial tables

---

## [0.1.0] — feat/auth-service/create-tables

### Added
- Auth service database schema and initial tables
- Initial microservice project structure

---
