package com.thanghub.courseservice.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Skip field null when serialize
public class ApiResponseBase<T> {

    private boolean success;
    private String message;
    private T data;
    private Object errors;

    public static <T> ApiResponseBase<T> ok(T data) {
        return ApiResponseBase.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponseBase<T> ok(String message, T data) {
        return ApiResponseBase.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponseBase<T> error(String message, Object errors) {
        return ApiResponseBase.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }

    public static <T> ApiResponseBase<T> fail(String message) {
        return ApiResponseBase.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
