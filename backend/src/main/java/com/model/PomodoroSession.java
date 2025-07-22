package com.model;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Component
@Scope("prototype") //原型模式，每次请求时重新创建
public class PomodoroSession {
    private final UUID sessionUUId = UUID.randomUUID();
    private final String sessionId = sessionUUId.toString();

    private TimerMode timerMode = TimerMode.CONTINUOUS;
    private boolean isRunning = false;

    private List<Integer> shortBreakTimes = new ArrayList<>();
    private int nextShortBreakIndex = 0;
    private TimerState curTimerState;
    private int remainingTime;

    private int longBreakDuration;
    private int shortBreakDuration;
    private int shortBreakMinInterval;
    private int shortBreakMaxInterval;
    private int workTime;
}
