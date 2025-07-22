package com.util;

import java.util.*;

public class RandomShortBreakListGenerator {

    /**
     * 生成休息时长列表（带种子的版本，便于测试）
     * @param seed 随机种子
     * @return 休息时间点列表
     */
    public List<Integer> generateShortBreakTimes(int continuousWorkTime, int minInterval, int maxInterval, long seed) {
        List<Integer> shortBreakTimes = new ArrayList<>();
        Random random = new Random(seed);
        int currentTime = 0;
        
        while (currentTime < continuousWorkTime) {
            int interval = random.nextInt(
                maxInterval - minInterval + 1
            ) + minInterval;
            
            currentTime += interval;
            
            if (currentTime < continuousWorkTime) {
                shortBreakTimes.add(currentTime);
            }
        }
        
        return shortBreakTimes;
    }
    
    /**
     * 将秒数转换为可读的时间格式
     * @param seconds 秒数
     * @return 格式化的时间字符串
     */
    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d分%d秒", minutes, remainingSeconds);
    }
    
    /**
     * 打印休息时间安排
     * @param shortBreakTimes 休息时间点列表
     */
    public void printBreakSchedule(List<Integer> shortBreakTimes) {
        System.out.println("=== 休息时间安排 ===");
        System.out.println();
        
        for (int i = 0; i < shortBreakTimes.size(); i++) {
            int time = shortBreakTimes.get(i);
            System.out.printf("第%d次休息: %d秒 (%s)\n", 
                i + 1, time, formatTime(time));
        }
        
        System.out.printf("\n总共安排了 %d 次休息\n", shortBreakTimes.size());
    }
    
    // 测试方法
    public static void main(String[] args) {
        RandomShortBreakListGenerator generator = new RandomShortBreakListGenerator();
        
        // 生成休息时间列表
        List<Integer> shortBreakTimes = generator.generateShortBreakTimes(90 * 60, 3 * 60, 5 * 60, 42L);
        
        // 打印结果
        generator.printBreakSchedule(shortBreakTimes);
        
        System.out.println("\n休息时间点列表: " + shortBreakTimes);
    }
}