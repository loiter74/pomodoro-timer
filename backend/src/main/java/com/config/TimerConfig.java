package com.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pomodoro")
@Data
public class TimerConfig {
    // 经典模式配置
    private int classicWorkTime = 25 * 60;
    private int classicShortBreakDuration = 5 * 60;
    private int classicLongBreakDuration = 15 * 60;

    // 连续模式配置
    private int continuousWorkTime = 90 * 60;
    private int continuousShortBreakDuration = 10;
    private int continuousLongBreakDuration = 20 * 60;
    private int continuousShortBreakMinInterval = 3 * 60;
    private int continuousShortBreakMaxInterval = 5 * 60;
}