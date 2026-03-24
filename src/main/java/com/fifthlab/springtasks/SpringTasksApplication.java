package com.fifthlab.springtasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringTasksApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringTasksApplication.class, args);
    }
}
