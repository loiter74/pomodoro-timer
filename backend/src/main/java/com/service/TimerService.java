package com.service;

import com.model.PomodoroSession;
import com.model.TimerMode;

public interface TimerService {
    void createTimer(String sessionId, TimerMode timerMode);
    void startTimer(String sessionId);
    void pauseTimer(String sessionId);
    void resetTimer(String sessionId);
    void deleteTimer(String sessionId);
    PomodoroSession getCurrentSession(String sessionId);
}
