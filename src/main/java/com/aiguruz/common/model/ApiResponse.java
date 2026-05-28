package com.aiguruz.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String  message;
    private T       data;
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> ok(T data) {
        var r = new ApiResponse<T>(); r.success = true; r.data = data; return r;
    }
    public static <T> ApiResponse<T> ok(T data, String msg) {
        var r = ok(data); r.message = msg; return r;
    }
    public static <T> ApiResponse<T> fail(String msg) {
        var r = new ApiResponse<T>(); r.success = false; r.message = msg; return r;
    }
}

