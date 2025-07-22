package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class PomodoroApp
{
    public static void main( String[] args )
    {
        SpringApplication.run(PomodoroApp.class, args);
        System.out.println("🍅 番茄钟启动成功！访问: http://localhost:8080");
    }
}
