package com.service;

import com.config.TimerConfig;
import com.model.PomodoroSession;
import com.model.TimerMode;
import com.model.TimerState;
import com.util.RandomShortBreakListGenerator;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Service
@Slf4j
public class TimerServiceImpl implements TimerService {

    @Autowired
    private TimerConfig timerConfig;

    @Autowired
    private ApplicationContext applicationContext; // 用于创建prototype bean

    // 使用ConcurrentHashMap管理多个会话
    private final Map<String, PomodoroSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ScheduledExecutorService> schedulers = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> currentTasks = new ConcurrentHashMap<>();

    // 休息时暂停上下文保存 - 每个会话独立
    private final Map<String, TimerState> statesBeforePause = new ConcurrentHashMap<>();
    private final Map<String, Long> breakStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> breakRemainingTimes = new ConcurrentHashMap<>();

    // 添加字段跟踪已消耗的休息时间
    private final Map<String, Integer> totalBreakTimeUsed = new ConcurrentHashMap<>();

    @Override
    public void createTimer(String sessionId, TimerMode timerMode) {
        if (sessions.containsKey(sessionId)) {
            throw new IllegalArgumentException("会话已存在: " + sessionId);
        }

        // 创建新的会话
        PomodoroSession session = new PomodoroSession();
        sessions.put(sessionId, session);

        // 为每个会话创建独立的调度器
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "Timer-" + sessionId.substring(0, Math.min(8, sessionId.length())));
            t.setDaemon(true); // 设置为守护线程
            return t;
        });
        schedulers.put(sessionId, scheduler);

        // 初始化会话
        initializeSession(sessionId, timerMode);

        log.info("🍅 [{}] 创建{}模式计时器",
                formatSessionId(sessionId),
                timerMode == TimerMode.CLASSIC ? "经典" : "连续学习");
    }

    private void initializeSession(String sessionId, TimerMode timerMode) {
        PomodoroSession session = sessions.get(sessionId);
        session.setTimerMode(timerMode);
        session.setCurTimerState(TimerState.PAUSED);
        session.setRunning(false);

        if (timerMode == TimerMode.CONTINUOUS) {
            // 连续模式配置
            session.setRemainingTime(timerConfig.getContinuousWorkTime());
            session.setWorkTime(timerConfig.getContinuousWorkTime());

            // 生成随机短休息时间点
            long seed = session.getSessionUUId().getMostSignificantBits() ^
                    session.getSessionUUId().getLeastSignificantBits();

            List<Integer> shortBreakTimes = new RandomShortBreakListGenerator().generateShortBreakTimes(
                    timerConfig.getContinuousWorkTime(),
                    timerConfig.getContinuousShortBreakMinInterval(),
                    timerConfig.getContinuousShortBreakMaxInterval(),
                    seed
            );

            session.setShortBreakTimes(shortBreakTimes);
            session.setShortBreakDuration(timerConfig.getContinuousShortBreakDuration());
            session.setLongBreakDuration(timerConfig.getContinuousLongBreakDuration());

            log.info("📋 [{}] 连续模式配置完成，短休息时间点: {}",
                    formatSessionId(sessionId), shortBreakTimes);
        } else {
            // 经典模式配置
            session.setRemainingTime(timerConfig.getClassicWorkTime());
            session.setWorkTime(timerConfig.getClassicWorkTime());
            session.setLongBreakDuration(timerConfig.getClassicLongBreakDuration());
            session.setShortBreakDuration(timerConfig.getClassicShortBreakDuration());
        }
    }

    @Override
    public void startTimer(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        // 如果当前存在暂停的计时器，则继续执行
        if (session.getCurTimerState() == TimerState.PAUSED && session.getRemainingTime() > 0) {
            resumeTimer(sessionId);
            return;
        }

        // 如果没有暂停的计时器，开始新的计时
        if (session.getCurTimerState() == TimerState.PAUSED ||
                session.getCurTimerState() == TimerState.COMPLETED) {
            session.setCurTimerState(TimerState.WORKING);
            // 如果是完成状态，需要重置时间
            if (session.getRemainingTime() <= 0) {
                if (session.getTimerMode() == TimerMode.CONTINUOUS) {
                    session.setRemainingTime(timerConfig.getContinuousWorkTime());
                } else {
                    session.setRemainingTime(timerConfig.getClassicWorkTime());
                }
                session.setNextShortBreakIndex(0);
            }
        }

        session.setRunning(true);
        startCountdown(sessionId);

        log.info("▶️ [{}] 开始计时，当前状态: {}, 剩余时间: {}秒",
                formatSessionId(sessionId),
                session.getCurTimerState(),
                session.getRemainingTime());
    }

    private void resumeTimer(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        if (!session.isRunning()) {
            session.setRunning(true);
            TimerState stateBeforePause = statesBeforePause.get(sessionId);

            // 🐛 修复：添加null检查
            if (stateBeforePause == null) {
                log.warn("⚠️ [{}] 暂停前状态丢失，默认为工作状态",
                        formatSessionId(sessionId));
                stateBeforePause = TimerState.WORKING;
            }

            if (stateBeforePause == TimerState.SHORT_BREAK) {
                // 恢复短休息
                session.setCurTimerState(TimerState.SHORT_BREAK);
                ScheduledExecutorService scheduler = schedulers.get(sessionId);

                // 🐛 修复：添加null检查
                Integer breakRemaining = breakRemainingTimes.get(sessionId);
                if (breakRemaining == null) {
                    log.warn("⚠️ [{}] 短休息剩余时间丢失，使用默认值",
                            formatSessionId(sessionId));
                    breakRemaining = session.getShortBreakDuration();
                }

                ScheduledFuture<?> task = scheduler.schedule(() -> endShortBreak(sessionId),
                        breakRemaining, TimeUnit.SECONDS);
                currentTasks.put(sessionId, task);

                log.info("▶️ [{}] 恢复短休息，剩余时间: {}秒",
                        formatSessionId(sessionId), breakRemaining);
            } else if (stateBeforePause == TimerState.LONG_BREAK) {
                // 恢复长休息
                session.setCurTimerState(TimerState.LONG_BREAK);

                // 🐛 修复：添加null检查
                Integer breakRemaining = breakRemainingTimes.get(sessionId);
                if (breakRemaining == null) {
                    log.warn("⚠️ [{}] 长休息剩余时间丢失，使用默认值",
                            formatSessionId(sessionId));
                    breakRemaining = session.getLongBreakDuration();
                }

                session.setRemainingTime(breakRemaining);
                startCountdown(sessionId);
                log.info("▶️ [{}] 恢复长休息，剩余时间: {}秒",
                        formatSessionId(sessionId), breakRemaining);
            } else {
                // 恢复工作状态
                session.setCurTimerState(TimerState.WORKING);
                startCountdown(sessionId);
                log.info("▶️ [{}] 恢复工作计时器，剩余时间: {}秒",
                        formatSessionId(sessionId), session.getRemainingTime());
            }
        }
    }


    private void startCountdown(String sessionId) {
        // 取消之前的任务
        ScheduledFuture<?> currentTask = currentTasks.get(sessionId);
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(false);
        }

        // 开始新的倒计时任务
        ScheduledExecutorService scheduler = schedulers.get(sessionId);
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> tick(sessionId), 0, 1, TimeUnit.SECONDS);
        currentTasks.put(sessionId, task);
    }

    private void tick(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        if (session == null || !session.isRunning()) {
            return;
        }

        int remainingTime = session.getRemainingTime();

        // 时间到了
        if (remainingTime <= 0) {
            handleTimeUp(sessionId);
            return;
        }

        // 检查是否需要短休息
        if (session.getCurTimerState() == TimerState.WORKING && session.getTimerMode() == TimerMode.CONTINUOUS) {
            checkShortBreak(sessionId, remainingTime);
        }

        // 减少剩余时间
        session.setRemainingTime(remainingTime - 1);

        // 每20秒打印一次日志
        if (remainingTime % 20 == 0) {
            log.debug("⏱️ [{}] 剩余时间: {}秒, 状态: {}",
                    formatSessionId(sessionId),
                    remainingTime,
                    session.getCurTimerState());
        }
    }

    /**
     * 检查是否需要短休息
     */
    private void checkShortBreak(String sessionId, int remainingTime) {
        PomodoroSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }

        // 计算实际已工作时间（不包括休息时间）
        int totalBreakUsed = totalBreakTimeUsed.getOrDefault(sessionId, 0);
        int actualWorkTime = session.getWorkTime() - remainingTime - totalBreakUsed;

        List<Integer> shortBreakTimes = session.getShortBreakTimes();
        int nextIndex = session.getNextShortBreakIndex();

        if (shortBreakTimes != null && nextIndex < shortBreakTimes.size()) {
            int nextBreakTime = shortBreakTimes.get(nextIndex);

            // 到达短休息时间
            if (actualWorkTime >= nextBreakTime) {
                startShortBreak(sessionId);
                session.setNextShortBreakIndex(nextIndex + 1);
            }
        }
    }

    /**
     * 开始短休息
     */
    private void startShortBreak(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }
        // 取消当前工作计时任务
        ScheduledFuture<?> currentTask = currentTasks.get(sessionId);
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(false);
            log.debug("🛑 [{}] 取消工作计时任务", formatSessionId(sessionId));
        }

        int breakDuration = session.getShortBreakDuration();
        breakStartTimes.put(sessionId, System.currentTimeMillis());
        session.setCurTimerState(TimerState.SHORT_BREAK);

        log.info("☕ [{}] 开始短休息，时长: {}秒",
                formatSessionId(sessionId), breakDuration);

        ScheduledExecutorService scheduler = schedulers.get(sessionId);
        ScheduledFuture<?> task = scheduler.schedule(() -> endShortBreak(sessionId), breakDuration, TimeUnit.SECONDS);
        currentTasks.put(sessionId, task);
    }

    /**
     * 结束短休息
     */
    private void endShortBreak(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        int currentBreakUsed = totalBreakTimeUsed.getOrDefault(sessionId, 0);
        totalBreakTimeUsed.put(sessionId, currentBreakUsed + session.getShortBreakDuration());


        session.setCurTimerState(TimerState.WORKING);
        startCountdown(sessionId);
        log.info("💪 [{}] 短休息结束，继续工作！剩余时间: {}秒",
                formatSessionId(sessionId),
                session.getRemainingTime());
    }

    /**
     * 处理时间到的情况
     */
    private void handleTimeUp(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        session.setRunning(false);

        ScheduledFuture<?> currentTask = currentTasks.get(sessionId);
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(false);
        }

        TimerState currentState = session.getCurTimerState();

        if (currentState == TimerState.WORKING) {
            // 工作时间结束
            handleWorkTimeUp(sessionId);
        } else if (currentState == TimerState.LONG_BREAK) {
            // 长休息结束
            handleLongBreakTimeUp(sessionId);
        }
    }

    /**
     * 工作时间结束处理
     */
    private void handleWorkTimeUp(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        log.info("🎉 [{}] 工作时间结束！", formatSessionId(sessionId));

        if (session.getTimerMode() == TimerMode.CLASSIC) {
            // 经典模式：开始长休息
            startLongBreak(sessionId);
        } else {
            // 连续模式：工作结束
            session.setCurTimerState(TimerState.COMPLETED);
            log.info("✅ [{}] 连续学习模式完成！", formatSessionId(sessionId));
        }
    }

    /**
     * 开始长休息
     */
    private void startLongBreak(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);

        session.setCurTimerState(TimerState.LONG_BREAK);
        session.setRemainingTime(session.getLongBreakDuration());
        session.setRunning(true);
        breakStartTimes.put(sessionId, System.currentTimeMillis());

        log.info("🌟 [{}] 开始长休息，时长: {}秒",
                formatSessionId(sessionId),
                session.getLongBreakDuration());

        startCountdown(sessionId);
    }

    /**
     * 长休息结束处理
     */
    private void handleLongBreakTimeUp(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        session.setCurTimerState(TimerState.COMPLETED);
        log.info("✅ [{}] 番茄钟周期完成！", formatSessionId(sessionId));
    }

    @Override
    public void pauseTimer(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        if (session.isRunning()) {
            // 保存暂停前的状态
            statesBeforePause.put(sessionId, session.getCurTimerState());

            // 计算休息剩余时间
            TimerState currentState = session.getCurTimerState();
            if (currentState == TimerState.SHORT_BREAK || currentState == TimerState.LONG_BREAK) {
                int remainingTime = calculateBreakRemainingTime(sessionId);
                breakRemainingTimes.put(sessionId, remainingTime);
            }

            session.setRunning(false);
            session.setCurTimerState(TimerState.PAUSED);

            ScheduledFuture<?> currentTask = currentTasks.get(sessionId);
            if (currentTask != null && !currentTask.isCancelled()) {
                currentTask.cancel(false);
            }

            TimerState stateBeforePause = statesBeforePause.get(sessionId);
            log.info("⏸️ [{}] 计时器已暂停，暂停前状态: {}, 剩余时间: {}秒",
                    formatSessionId(sessionId),
                    stateBeforePause,
                    stateBeforePause == TimerState.WORKING ? session.getRemainingTime() : breakRemainingTimes.get(sessionId));
        }
    }

    /**
     * 计算休息剩余时间
     */
    private int calculateBreakRemainingTime(String sessionId) {
        // 优先使用ScheduledFuture的剩余时间
        ScheduledFuture<?> currentTask = currentTasks.get(sessionId);
        if (currentTask != null && !currentTask.isCancelled()) {
            long delay = currentTask.getDelay(TimeUnit.SECONDS);
            return (int) Math.max(0, delay);
        }

        // ✅ 修复：添加session null检查
        PomodoroSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("⚠️ [{}] 会话不存在，无法计算休息剩余时间", formatSessionId(sessionId));
            return 0;
        }

        // 备用方案：通过开始时间计算
        Long breakStartTime = breakStartTimes.get(sessionId);
        if (breakStartTime != null && breakStartTime > 0) {
            long elapsedSeconds = (System.currentTimeMillis() - breakStartTime) / 1000;
            TimerState currentState = session.getCurTimerState();
            int totalDuration = currentState == TimerState.SHORT_BREAK ?
                    session.getShortBreakDuration() : session.getLongBreakDuration();
            return Math.max(0, totalDuration - (int) elapsedSeconds);
        }

        // 保守方案：返回完整时长
        TimerState currentState = session.getCurTimerState();
        return currentState == TimerState.SHORT_BREAK ?
                session.getShortBreakDuration() : session.getLongBreakDuration();
    }

    @Override
    public void resetTimer(String sessionId) {
        PomodoroSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        totalBreakTimeUsed.remove(sessionId);

        // 停止当前计时
        session.setRunning(false);

        ScheduledFuture<?> currentTask = currentTasks.get(sessionId);
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(false);
        }

        // 清理暂停状态
        statesBeforePause.remove(sessionId);
        breakRemainingTimes.remove(sessionId);
        breakStartTimes.remove(sessionId);

        // 重置到初始状态
        if (session.getTimerMode() == TimerMode.CONTINUOUS) {
            session.setRemainingTime(timerConfig.getContinuousWorkTime());
            session.setWorkTime(timerConfig.getContinuousWorkTime()); // 🐛 修复：重新设置工作时间

            // 🐛 修复：重新生成短休息时间点
            long seed = session.getSessionUUId().getMostSignificantBits() ^
                    session.getSessionUUId().getLeastSignificantBits();

            List<Integer> shortBreakTimes = new RandomShortBreakListGenerator().generateShortBreakTimes(
                    timerConfig.getContinuousWorkTime(),
                    timerConfig.getContinuousShortBreakMinInterval(),
                    timerConfig.getContinuousShortBreakMaxInterval(),
                    seed
            );
            session.setShortBreakTimes(shortBreakTimes);
        } else {
            session.setRemainingTime(timerConfig.getClassicWorkTime());
            session.setWorkTime(timerConfig.getClassicWorkTime());
        }

        session.setCurTimerState(TimerState.PAUSED);
        session.setNextShortBreakIndex(0);

        log.info("🔄 [{}] 计时器已重置",formatSessionId(sessionId));
    }

    @Override
    public void deleteTimer(String sessionId) {
        try {
            // 停止计时器
            PomodoroSession session = sessions.get(sessionId);
            if (session != null) {
                session.setRunning(false);
            }

            // 取消任务
            ScheduledFuture<?> currentTask = currentTasks.get(sessionId);
            if (currentTask != null && !currentTask.isCancelled()) {
                currentTask.cancel(true);
            }

            // 🐛 修复：安全关闭调度器
            ScheduledExecutorService scheduler = schedulers.get(sessionId);
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    // 等待最多5秒让任务完成
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                        // 再等待最多5秒
                        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                            log.warn("⚠️ [{}] 调度器未能正常关闭",
                                    formatSessionId(sessionId));
                        }
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // 清理所有相关数据
            sessions.remove(sessionId);
            schedulers.remove(sessionId);
            currentTasks.remove(sessionId);
            statesBeforePause.remove(sessionId);
            breakStartTimes.remove(sessionId);
            breakRemainingTimes.remove(sessionId);
            totalBreakTimeUsed.remove(sessionId);

            log.info("🗑️ [{}] 计时器已删除", formatSessionId(sessionId));
        } catch (Exception e) {
            log.error("删除计时器时发生异常: " + sessionId, e);
        }
    }

    private String formatSessionId(String sessionId) {
        return sessionId.substring(0, Math.min(8, sessionId.length()));
    }

    @Override
    public PomodoroSession getCurrentSession(String sessionId) {
        return sessions.get(sessionId);
    }

    @PreDestroy
    public void cleanup() {
        try {
            // 🐛 修复：使用副本避免并发修改异常
            Set<String> sessionIds = new HashSet<>(sessions.keySet());
            for (String sessionId : sessionIds) {
                try {
                    deleteTimer(sessionId);
                } catch (Exception e) {
                    log.error("清理会话时发生异常: " + sessionId, e);
                }
            }
            log.info("🧹 所有计时器资源已清理");
        } catch (Exception e) {
            log.error("清理资源时发生异常", e);
        }
    }

}



