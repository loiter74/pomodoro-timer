package com.controller;

import com.model.PomodoroSession;
import com.model.TimerMode;
import com.model.TimerState;
import com.service.TimerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pomodoro")
@CrossOrigin(origins = "*") // 允许跨域，生产环境请配置具体域名
@Slf4j
public class TimerController {

    @Autowired
    private TimerService timerService;

    /**
     * 创建计时器
     * POST /api/timer/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTimer(@RequestBody CreateTimerRequest request) {
        try {
            // 验证请求参数
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("会话ID不能为空"));
            }

            if (request.getTimerMode() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("计时器模式不能为空"));
            }

            timerService.createTimer(request.getSessionId(), request.getTimerMode());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "计时器创建成功");
            response.put("sessionId", request.getSessionId());
            response.put("timerMode", request.getTimerMode());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("创建计时器失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("创建计时器时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 启动计时器
     * POST /api/timer/{sessionId}/start
     */
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<Map<String, Object>> startTimer(@PathVariable String sessionId) {
        try {
            timerService.startTimer(sessionId);

            PomodoroSession session = timerService.getCurrentSession(sessionId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "计时器已启动");
            response.put("sessionId", sessionId);
            response.put("currentState", session.getCurTimerState());
            response.put("remainingTime", session.getRemainingTime());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("启动计时器失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("启动计时器时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 暂停计时器
     * POST /api/timer/{sessionId}/pause
     */
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<Map<String, Object>> pauseTimer(@PathVariable String sessionId) {
        try {
            timerService.pauseTimer(sessionId);

            PomodoroSession session = timerService.getCurrentSession(sessionId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "计时器已暂停");
            response.put("sessionId", sessionId);
            response.put("currentState", session.getCurTimerState());
            response.put("remainingTime", session.getRemainingTime());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("暂停计时器失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("暂停计时器时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 重置计时器
     * POST /api/timer/{sessionId}/reset
     */
    @PostMapping("/{sessionId}/reset")
    public ResponseEntity<Map<String, Object>> resetTimer(@PathVariable String sessionId) {
        try {
            timerService.resetTimer(sessionId);

            PomodoroSession session = timerService.getCurrentSession(sessionId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "计时器已重置");
            response.put("sessionId", sessionId);
            response.put("currentState", session.getCurTimerState());
            response.put("remainingTime", session.getRemainingTime());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("重置计时器失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("重置计时器时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 删除计时器
     * DELETE /api/timer/{sessionId}
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteTimer(@PathVariable String sessionId) {
        try {
            timerService.deleteTimer(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "计时器已删除");
            response.put("sessionId", sessionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("删除计时器时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 获取计时器状态
     * GET /api/timer/{sessionId}/status
     */
    @GetMapping("/{sessionId}/status")
    public ResponseEntity<Map<String, Object>> getTimerStatus(@PathVariable String sessionId) {
        try {
            PomodoroSession session = timerService.getCurrentSession(sessionId);

            if (session == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("timerMode", session.getTimerMode());
            response.put("currentState", session.getCurTimerState());
            response.put("remainingTime", session.getRemainingTime());
            response.put("workTime", session.getWorkTime());
            response.put("isRunning", session.isRunning());
            response.put("shortBreakDuration", session.getShortBreakDuration());
            response.put("longBreakDuration", session.getLongBreakDuration());

            // 连续模式特有信息
            if (session.getTimerMode() == TimerMode.CONTINUOUS) {
                response.put("shortBreakTimes", session.getShortBreakTimes());
                response.put("nextShortBreakIndex", session.getNextShortBreakIndex());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取计时器状态时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 获取计时器详细信息（包含格式化时间）
     * GET /api/timer/{sessionId}/info
     */
    @GetMapping("/{sessionId}/info")
    public ResponseEntity<Map<String, Object>> getTimerInfo(@PathVariable String sessionId) {
        try {
            PomodoroSession session = timerService.getCurrentSession(sessionId);

            if (session == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("timerMode", session.getTimerMode());
            response.put("currentState", session.getCurTimerState());
            response.put("remainingTime", session.getRemainingTime());
            response.put("remainingTimeFormatted", formatTime(session.getRemainingTime()));
            response.put("workTime", session.getWorkTime());
            response.put("workTimeFormatted", formatTime(session.getWorkTime()));
            response.put("isRunning", session.isRunning());
            response.put("shortBreakDuration", session.getShortBreakDuration());
            response.put("shortBreakDurationFormatted", formatTime(session.getShortBreakDuration()));
            response.put("longBreakDuration", session.getLongBreakDuration());
            response.put("longBreakDurationFormatted", formatTime(session.getLongBreakDuration()));

            // 计算进度百分比
            if (session.getWorkTime() > 0) {
                double progress = ((double) (session.getWorkTime() - session.getRemainingTime()) / session.getWorkTime()) * 100;
                response.put("progressPercentage", Math.round(progress * 100.0) / 100.0);
            }

            // 连续模式特有信息
            if (session.getTimerMode() == TimerMode.CONTINUOUS) {
                response.put("shortBreakTimes", session.getShortBreakTimes());
                response.put("nextShortBreakIndex", session.getNextShortBreakIndex());

                // 下一个短休息时间
                if (session.getShortBreakTimes() != null &&
                        session.getNextShortBreakIndex() < session.getShortBreakTimes().size()) {
                    int nextBreakTime = session.getShortBreakTimes().get(session.getNextShortBreakIndex());
                    response.put("nextShortBreakTime", nextBreakTime);
                    response.put("nextShortBreakTimeFormatted", formatTime(nextBreakTime));
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取计时器信息时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 健康检查
     * GET /api/timer/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Timer service is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 格式化时间（秒 -> MM:SS）
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    // ========== 请求/响应 DTO ==========

    /**
     * 创建计时器请求
     */
    public static class CreateTimerRequest {
        private String sessionId;
        private TimerMode timerMode;

        // Getters and Setters
        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public TimerMode getTimerMode() {
            return timerMode;
        }

        public void setTimerMode(TimerMode timerMode) {
            this.timerMode = timerMode;
        }
    }
}
