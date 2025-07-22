package com.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理 HTTP 方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("HTTP方法不支持: {} - 支持的方法: {}", e.getMethod(), e.getSupportedMethods());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "HTTP方法不支持");
        response.put("message", String.format("请求方法 '%s' 不被支持", e.getMethod()));
        response.put("supportedMethods", e.getSupportedMethods());
        response.put("suggestion", getSuggestion(e.getMethod()));
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 处理参数错误
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "参数错误");
        response.put("message", e.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("服务器内部错误", e);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "服务器内部错误");
        response.put("message", "请联系系统管理员");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 根据错误的HTTP方法提供建议
     */
    private String getSuggestion(String method) {
        return switch (method.toUpperCase()) {
            case "GET" -> "如果要获取数据，请使用 GET /api/timer/{sessionId}/status 或 /api/timer/{sessionId}/info";
            case "POST" -> "如果要执行操作，请检查URL路径是否正确，如 /api/timer/create 或 /api/timer/{sessionId}/start";
            case "PUT" -> "PUT方法当前不被支持，请使用POST方法进行操作";
            case "PATCH" -> "PATCH方法当前不被支持，请使用POST方法进行操作";
            default -> "请查看API文档了解支持的HTTP方法";
        };
    }
}
