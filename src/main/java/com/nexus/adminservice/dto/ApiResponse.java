package com.nexus.adminservice.dto;

import java.time.Instant;

/**
 * Standard response envelope used across all Nexus services:
 * { success, data, error, timestamp } — see Backend Plan §6.
 */
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;
    private Instant timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.error = null;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.data = null;
        response.error = message;
        return response;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getError() { return error; }
    public Instant getTimestamp() { return timestamp; }
}
