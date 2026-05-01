package com.thanghub.courseservice.course;

import com.thanghub.common.enums.CourseStatusEnum;
import com.thanghub.common.enums.LevelEnum;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    public static Specification<Course> titleContains(String title) {
        return (root, query, cb) ->
                title == null || title.isBlank()
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Course> hasStatus(CourseStatusEnum status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Course> hasLevel(LevelEnum level) {
        return (root, query, cb) ->
                level == null ? cb.conjunction() : cb.equal(root.get("level"), level);
    }
}
